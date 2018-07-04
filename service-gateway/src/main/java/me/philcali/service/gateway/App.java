package me.philcali.service.gateway;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.amazonaws.services.apigateway.AmazonApiGateway;
import com.amazonaws.services.apigateway.AmazonApiGatewayClientBuilder;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.CreateRestApiResult;
import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.ResourceConflictException;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IModule;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class App {

    @Parameter(names = { "--jar", "-j" }, required = true)
    private String jarFile;

    @Parameter(names = { "--iam-role", "-i" }, required = true)
    private String iamRole;

    @Parameter(names = { "--service", "-s" }, required = true)
    private String serviceName;

    @Parameter(names = { "--bucket", "-b" }, required = true)
    private String s3BucketName;

    @Parameter(names = { "--prefix", "-p" }, required = false)
    private String s3KeyPrefix = "lambdaFunctions";

    @Parameter(names = { "--region", "-r" }, required = false)
    private String regionName;

    @Parameter(names = { "--name", "-n" }, required = false)
    private String functionName = "ServiceFunction";

    @Parameter(names = { "--timeout", "-t" }, required = false)
    private int timeout = 10;

    @Parameter(names = { "--memory", "-m" }, required = false)
    private int memorySize = 512;

    public static void main(final String[] args) {
        final App app = new App();
        JCommander.newBuilder()
                .addObject(app)
                .acceptUnknownOptions(false)
                .build()
                .parse(args);
        app.run();
    }

    public void run() {
        final AmazonApiGateway gateway = AmazonApiGatewayClientBuilder.defaultClient();
        final String restApiId = getRestApiId(gateway);
    }

    private Map<String, Map<String, ResourceMethod>> getResourceLookupTable() {
        final List<ResourceMethod> methods = getResourceMethods();
        Collections.sort(methods, new ResourceMethodComparator());
        final Map<String, Map<String, ResourceMethod>> lookup = new LinkedHashMap<>();
        methods.forEach(method -> lookup.compute(method.getPatternPath(), (path, temp) -> {
            final Map<String, ResourceMethod> t = Optional.ofNullable(temp).orElseGet(HashMap::new);
            t.put(method.getMethod(), method);
            return t;
        }));
        return lookup;
    }

    private String getRestApiId(final AmazonApiGateway gateway) {
        final CreateRestApiResult createApi = gateway.createRestApi(new CreateRestApiRequest()
                .withName(serviceName));
        return createApi.getId();
    }

    private List<ResourceMethod> getResourceMethods() {
        final ClassLoader loader = new URLClassLoader(getModuleURLs(), ClassLoader.getSystemClassLoader());
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class, loader);
        final List<Object> components = StreamSupport.stream(moduleLoader.spliterator(), false)
                .collect(Collectors.toList());
        final RequestRouter router = ReflectiveResourceRouter.builder()
                .withComponents(components)
                .withCollector(new DefaultResourceMethodCollector(new ObjectMarshallerJackson()))
                .build();
        return router.getResourceMethods();
    }

    private FunctionCode getFunctionCode() throws IOException {
        final Path jarPath = Paths.get(jarFile);
        final String s3Key = new StringJoiner("/")
                .add(s3KeyPrefix)
                .add(jarPath.toFile().getName())
                .toString();
        return new FunctionCode()
                .withS3Bucket(s3BucketName)
                .withS3Key(s3Key)
                .withZipFile(ByteBuffer.wrap(Files.readAllBytes(jarPath)));
    }

    private void createOrUpdateFunction(final String s3Key) throws IOException {
        final AWSLambda lambda = AWSLambdaClientBuilder.defaultClient();
        try {
            lambda.createFunction(new CreateFunctionRequest()
                    .withCode(getFunctionCode())
                    .withFunctionName(functionName)
                    .withHandler("me.philcali.service.function.ServiceFunction::handleRequest")
                    .withRuntime(Runtime.Java8)
                    .withTimeout(timeout)
                    .withMemorySize(memorySize)
                    .withRole(iamRole));
        } catch (ResourceConflictException e) {
            final FunctionCode code = getFunctionCode();
            lambda.updateFunctionCode(new UpdateFunctionCodeRequest()
                    .withFunctionName(functionName)
                    .withS3Bucket(code.getS3Bucket())
                    .withS3Key(code.getS3Key())
                    .withZipFile(code.getZipFile()));
        }
    }

    private URL[] getModuleURLs() {
        try {
            return new URL[] { new File(jarFile).toURI().toURL() };
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
