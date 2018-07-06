package me.philcali.service.gateway;

import java.util.Arrays;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import me.philcali.service.gateway.lambda.ServerlessFunction;
import me.philcali.service.gateway.resource.ResourceMethodLoader;

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

    @Parameter(names = { "--authorizer", "-a" }, required = false)
    private String authorizerName = "CustomAuthorizer";

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
        Arrays.asList(functionName, authorizerName).forEach(function -> {
            System.out.println(String.format("Uploading %s to %s", jarFile, function));
            final ServerlessFunction serviceFunction = ServerlessFunction.builder()
                    .withFunctionCode(jarFile)
                    .withFunctionName(function)
                    .withServiceName(serviceName)
                    .withRegion(regionName)
                    .withS3Prefix(s3KeyPrefix)
                    .withS3BucketName(s3BucketName)
                    .withTimeout(timeout)
                    .withMemorySize(memorySize)
                    .withIamRole(iamRole)
                    .build();
            serviceFunction.upsert();
        });
        final ResourceMethodLoader loader = new ResourceMethodLoader(jarFile);
        loader.getMethods().forEach((path, methods) -> {
            System.out.println(path + " -> " + methods);
        });
    }
}
