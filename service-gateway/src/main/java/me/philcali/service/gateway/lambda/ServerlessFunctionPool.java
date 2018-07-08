package me.philcali.service.gateway.lambda;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaAsyncClientBuilder;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;

import me.philcali.service.gateway.AppContext;
import me.philcali.service.gateway.identity.IUserPool;
import me.philcali.service.gateway.identity.IdentityPool;

public class ServerlessFunctionPool implements IFunctionPool {

    public static final class Builder {
        private AppContext context;
        private IUserPool identityPool;
        private AWSLambda lambda;

        public Builder withLambda(final AWSLambda lambda) {
            this.lambda = lambda;
            return this;
        }

        public Builder withUserPool(final IUserPool identityPool) {
            this.identityPool = identityPool;
            return this;
        }

        public Builder withContext(final AppContext context) {
            this.context = context;
            return this;
        }

        public ServerlessFunctionPool build() {
            Objects.requireNonNull(context, "App context is required!");
            createIdentityPool();
            createLambdaClient();
            return new ServerlessFunctionPool(this);
        }

        private void createIdentityPool() {
            if (Objects.isNull(identityPool)) {
                identityPool = new IdentityPool(context.getRegionName());
            }
        }

        private void createLambdaClient() {
            if (Objects.isNull(lambda)) {
                lambda = Optional.ofNullable(context.getRegionName())
                        .map(r -> AWSLambdaClientBuilder.standard().withRegion(r).build())
                        .orElseGet(AWSLambdaAsyncClientBuilder::defaultClient);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Map<String, ServerlessFunction> cache;
    private final AWSLambda lambda;
    private final IUserPool identityPool;
    private final AppContext context;

    private ServerlessFunctionPool(final Builder builder) {
        this.identityPool = builder.identityPool;
        this.lambda = builder.lambda;
        this.context = builder.context;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public ServerlessFunction getFunction(final String functionName) {
        return cache.computeIfAbsent(functionName, funcName -> {
            final ServerlessFunction serviceFunction = ServerlessFunction.builder()
                    .withContext(context)
                    .withLambda(lambda)
                    .withFunctionName(funcName)
                    .withRoleArn(identityPool.getRole(context.getIamRole()).getArn())
                    .build();
            serviceFunction.upsert();
            return serviceFunction;
        });
    }
}
