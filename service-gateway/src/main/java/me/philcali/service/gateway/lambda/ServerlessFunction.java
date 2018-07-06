package me.philcali.service.gateway.lambda;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.ResourceConflictException;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;

public class ServerlessFunction {
    public static final class Builder {
        private String serviceName;
        private String functionName;
        private String functionCode;
        private String iamRole;
        private int timeout;
        private int memorySize;
        private String region;
        private AWSLambda lambda;

        public Builder withRegion(final String region) {
            this.region = region;
            return this;
        }

        public Builder withServiceName(final String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        public Builder withFunctionName(final String functionName) {
            this.functionName = functionName;
            return this;
        }

        public Builder withFunctionCode(final String functionCode) {
            this.functionCode = functionCode;
            return this;
        }

        public Builder withTimeout(final int timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder withMemorySize(final int memorySize) {
            this.memorySize = memorySize;
            return this;
        }

        public Builder withIamRole(final String iamRole) {
            this.iamRole = iamRole;
            return this;
        }

        public ServerlessFunction build() {
            Objects.requireNonNull(iamRole, "Must supply an iam role");
            Objects.requireNonNull(serviceName, "Must supply a service name");
            Objects.requireNonNull(functionCode, "Must supply a function source");
            Objects.requireNonNull(functionName, "Must supply a function name");
            createLambdaClient();
            return new ServerlessFunction(this);
        }

        private void createLambdaClient() {
            if (Objects.isNull(lambda)) {
                lambda = Optional.ofNullable(region)
                        .map(r -> AWSLambdaClientBuilder.standard().withRegion(r).build())
                        .orElseGet(AWSLambdaClientBuilder::defaultClient);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final String serviceName;
    private final String functionName;
    private final String functionCode;
    private final String iamRole;
    private final int timeout;
    private final int memorySize;
    private AWSLambda lambda;

    public ServerlessFunction(final Builder builder) {
        this.functionCode = builder.functionCode;
        this.functionName = builder.functionName;
        this.iamRole = builder.iamRole;
        this.timeout = builder.timeout;
        this.memorySize = builder.memorySize;
        this.lambda = builder.lambda;
        this.serviceName = builder.serviceName;
    }

    public void upsert() {
        try {
            final String lambdaFunctionName = serviceName + functionName;
            final FunctionCode code = getFunctionCode();
            try {
                lambda.createFunction(new CreateFunctionRequest()
                        .withCode(getFunctionCode())
                        .withFunctionName(lambdaFunctionName)
                        .withDescription(String.format("Service function for %s", serviceName))
                        .withHandler(String.format("me.philcali.service.function.%s::handleRequest", functionName))
                        .withRuntime(Runtime.Java8)
                        .withTimeout(timeout)
                        .withMemorySize(memorySize)
                        .withRole(iamRole));
            } catch (ResourceConflictException e) {
                lambda.updateFunctionConfiguration(new UpdateFunctionConfigurationRequest()
                        .withTimeout(timeout)
                        .withMemorySize(memorySize));
                lambda.updateFunctionCode(new UpdateFunctionCodeRequest()
                        .withFunctionName(lambdaFunctionName)
                        .withZipFile(code.getZipFile()));
            }
        } catch (IOException ie) {
            throw new ServiceFunctionException(ie);
        }
    }

    private FunctionCode getFunctionCode() throws IOException {
        final Path jarPath = Paths.get(functionCode);
        return new FunctionCode().withZipFile(ByteBuffer.wrap(Files.readAllBytes(jarPath)));
    }
}
