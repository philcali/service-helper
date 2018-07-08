package me.philcali.service.gateway.lambda;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.model.AddPermissionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.GetFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.GetFunctionConfigurationResult;
import com.amazonaws.services.lambda.model.ResourceConflictException;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;

import me.philcali.service.gateway.AppContext;

public class ServerlessFunction {
    public static final class Builder {
        private String functionName;
        private AppContext context;
        private String roleArn;
        private AWSLambda lambda;

        public Builder withContext(final AppContext context) {
            this.context = context;
            return this;
        }

        public Builder withFunctionName(final String functionName) {
            this.functionName = functionName;
            return this;
        }

        public Builder withLambda(final AWSLambda lambda) {
            this.lambda = lambda;
            return this;
        }

        public Builder withRoleArn(final String roleArn) {
            this.roleArn = roleArn;
            return this;
        }

        public ServerlessFunction build() {
            Objects.requireNonNull(context, "Must supply an app context");
            Objects.requireNonNull(functionName, "Must supply a function name");
            Objects.requireNonNull(lambda, "Must supply a lambda client!");
            Objects.requireNonNull(roleArn, "Role ARN must exists!");
            return new ServerlessFunction(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String functionName;
    private final String roleArn;
    private final AppContext context;
    private final AWSLambda lambda;

    public ServerlessFunction(final Builder builder) {
        this.functionName = builder.functionName;
        this.lambda = builder.lambda;
        this.context = builder.context;
        this.roleArn = builder.roleArn;
    }

    public void upsert() {
        try {
            final String lambdaFunctionName = context.getServiceName() + functionName;
            final FunctionCode code = getFunctionCode();
            try {
                lambda.createFunction(new CreateFunctionRequest()
                        .withCode(getFunctionCode())
                        .withFunctionName(lambdaFunctionName)
                        .withDescription(String.format("Service function for %s", context.getServiceName()))
                        .withHandler(String.format("me.philcali.service.function.%s::handleRequest", functionName))
                        .withRuntime(Runtime.Java8)
                        .withTimeout(context.getTimeout())
                        .withMemorySize(context.getMemorySize())
                        .withRole(roleArn));
            } catch (ResourceConflictException e) {
                lambda.updateFunctionConfiguration(new UpdateFunctionConfigurationRequest()
                        .withFunctionName(lambdaFunctionName)
                        .withTimeout(context.getTimeout())
                        .withMemorySize(context.getMemorySize()));
                lambda.updateFunctionCode(new UpdateFunctionCodeRequest()
                        .withFunctionName(lambdaFunctionName)
                        .withZipFile(code.getZipFile()));
            }
        } catch (IOException ie) {
            throw new ServiceFunctionException(ie);
        }
    }

    public String getArn() {
        final GetFunctionConfigurationResult result = lambda.getFunctionConfiguration(new GetFunctionConfigurationRequest()
                .withFunctionName(context.getServiceName() + functionName));
        return result.getFunctionArn();
    }

    public void addPermission(final String sourceArn) {
        try {
            lambda.addPermission(new AddPermissionRequest()
                    .withFunctionName(context.getServiceName() + functionName)
                    .withStatementId(context.getServiceName() + "Invocations")
                    .withPrincipal("apigateway.amazonaws.com")
                    .withSourceArn(sourceArn)
                    .withAction("lambda:InvokeFunction"));
        } catch (ResourceConflictException rce) {
            // ignore
        }
    }

    private FunctionCode getFunctionCode() throws IOException {
        final Path jarPath = Paths.get(context.getJarFile());
        return new FunctionCode().withZipFile(ByteBuffer.wrap(Files.readAllBytes(jarPath)));
    }
}
