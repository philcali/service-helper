package me.philcali.service.gateway.lambda;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.ResourceConflictException;
import com.amazonaws.services.lambda.model.Runtime;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

public class ServerlessFunction {
    public static final class Builder {
        private String serviceName;
        private String functionName;
        private String functionCode;
        private String iamRole;
        private String s3Prefix;
        private String s3BucketName;
        private int timeout;
        private int memorySize;
        private String region;
        private AWSLambda lambda;
        private AmazonS3 s3;

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

        public Builder withS3BucketName(final String s3BucketName) {
            this.s3BucketName = s3BucketName;
            return this;
        }

        public Builder withS3Prefix(final String s3Prefix) {
            this.s3Prefix = s3Prefix;
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
            Objects.requireNonNull(s3BucketName, "Must supply a s3 bucket name");
            Objects.requireNonNull(serviceName, "Must supply a service name");
            Objects.requireNonNull(functionCode, "Must supply a function source");
            Objects.requireNonNull(functionName, "Must supply a function name");
            createLambdaClient();
            createS3Client();
            return new ServerlessFunction(this);
        }

        private void createLambdaClient() {
            if (Objects.isNull(lambda)) {
                lambda = Optional.ofNullable(region)
                        .map(r -> AWSLambdaClientBuilder.standard().withRegion(r).build())
                        .orElseGet(AWSLambdaClientBuilder::defaultClient);
            }
        }

        private void createS3Client() {
            if (Objects.isNull(s3)) {
                s3 = Optional.ofNullable(region)
                        .map(r -> AmazonS3ClientBuilder.standard().withRegion(r).build())
                        .orElseGet(AmazonS3ClientBuilder::defaultClient);
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
    private final String s3Prefix;
    private final String s3BucketName;
    private final int timeout;
    private final int memorySize;
    private AWSLambda lambda;
    private AmazonS3 s3;

    public ServerlessFunction(final Builder builder) {
        this.functionCode = builder.functionCode;
        this.functionName = builder.functionName;
        this.iamRole = builder.iamRole;
        this.timeout = builder.timeout;
        this.memorySize = builder.memorySize;
        this.s3 = builder.s3;
        this.lambda = builder.lambda;
        this.s3Prefix = builder.s3Prefix;
        this.s3BucketName = builder.s3BucketName;
        this.serviceName = builder.serviceName;
    }

    public void upsert() {
        try {
            try {
                lambda.createFunction(new CreateFunctionRequest()
                        .withCode(getFunctionCode())
                        .withFunctionName(functionName)
                        .withHandler(String.format("me.philcali.service.function.%s::handleRequest", functionName))
                        .withRuntime(Runtime.Java8)
                        .withTimeout(timeout)
                        .withMemorySize(memorySize)
                        .withRole(iamRole));
            } catch (ResourceConflictException e) {
                final FunctionCode code = getFunctionCode();
                lambda.updateFunctionCode(new UpdateFunctionCodeRequest()
                        .withFunctionName(serviceName + functionName)
                        .withS3Bucket(code.getS3Bucket())
                        .withS3Key(code.getS3Key())
                        .withZipFile(code.getZipFile()));
            }
        } catch (IOException ie) {
            throw new ServiceFunctionException(ie);
        }
    }

    private FunctionCode getFunctionCode() throws IOException {
        if (!s3.doesBucketExistV2(s3BucketName)) {
            s3.createBucket(s3BucketName);
        }
        final Path jarPath = Paths.get(functionCode);
        final String s3Key = new StringJoiner("/")
                .add(s3Prefix)
                .add(jarPath.toFile().getName())
                .toString();
        return new FunctionCode()
                .withS3Bucket(s3BucketName)
                .withS3Key(s3Key)
                .withZipFile(ByteBuffer.wrap(Files.readAllBytes(jarPath)));
    }
}
