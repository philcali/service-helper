package me.philcali.service.function.auth;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Policy implements IPolicy {
    private static final int REGION_INDEX = 3;
    private static final int STAGE_INDEX = 1;
    private static final int ACCOUNT_INDEX = 4;
    private static final int GATEWAY_INDEX = 5;
    private static final int API_INDEX = 0;

    private static final String RESOURCE_PATTERN = "arn:aws:execute-api:%s:%s:%s/%s/%s/%s";
    private static final String ACTION = "execute-api:Invoke";
    private static final String VERSION = "2012-10-17";

    public static class Builder {
        private String principalId;
        private Map<String, Object> policyDocument;
        private Map<String, Object> context;
        private List<String> allowMethods = new ArrayList<>();
        private List<String> denyMethods = new ArrayList<>();
        private String region;
        private String accountId;
        private String gatewayId;
        private String stage;

        public Builder withRegion(final String region) {
            this.region = region;
            return this;
        }

        public Builder withAccountId(final String accountId) {
            this.accountId = accountId;
            return this;
        }

        public Builder withGatewayId(final String gatewayId) {
            this.gatewayId = gatewayId;
            return this;
        }

        public Builder withStage(final String stage) {
            this.stage = stage;
            return this;
        }

        public Builder withMethodArn(final String methodArn) {
            final String[] parts = methodArn.split(":");
            final String[] gateway = parts[GATEWAY_INDEX].split("/");
            this.region = parts[REGION_INDEX];
            this.accountId = parts[ACCOUNT_INDEX];
            this.gatewayId = gateway[API_INDEX];
            this.stage = gateway[STAGE_INDEX];
            return this;
        }

        public Builder withPrincipalId(final String principalId) {
            this.principalId = principalId;
            return this;
        }

        public Builder withContext(final String key, final Object value) {
            if (context == null) {
                context = new HashMap<>();
            }
            context.put(key, value);
            return this;
        }

        public Builder withContext(final Map<String, Object> context) {
            this.context = context;
            return this;
        }

        public Builder allowMethod(final String method, final String resource) {
            return addMethod(allowMethods, method, resource);
        }

        public Builder denyMethod(final String method, final String resource) {
            return addMethod(denyMethods, method, resource);
        }

        public Builder allowAllMethods() {
            return allowMethod("*", "*");
        }

        public Builder denyAllMethods() {
            return denyMethod("*", "*");
        }

        private List<Map<String, Object>> statementsForEffect(final String effect, final List<String> methods) {
            if (methods.isEmpty()) {
                return Collections.emptyList();
            }

            final Map<String, Object> statement = new HashMap<>();
            statement.put("Action", ACTION);
            statement.put("Effect", effect);
            statement.put("Resource", methods);
            return Arrays.asList(statement);
        }

        private Builder addMethod(final List<String> methods, final String method, final String resource) {
            Objects.requireNonNull(region, "A region name must be set!");
            Objects.requireNonNull(accountId, "An AWS account Id must be set!");
            Objects.requireNonNull(gatewayId, "An API gateway Id must be set!");
            Objects.requireNonNull(stage, "A valid API stage must be set!");
            methods.add(String.format(RESOURCE_PATTERN, region, accountId, gatewayId, stage, method, resource));
            return this;
        }

        private Builder constructPolicyDocument() {
            this.policyDocument = new HashMap<>();
            final List<Map<String, Object>> statements = new ArrayList<>();
            statements.addAll(statementsForEffect("Allow", allowMethods));
            statements.addAll(statementsForEffect("Deny", denyMethods));
            policyDocument.put("Version", VERSION);
            policyDocument.put("Statement", statements);
            return this;
        }

        public IPolicy build() {
            Objects.requireNonNull(principalId, "The principalId must be set!");
            if (allowMethods.isEmpty() && denyMethods.isEmpty()) {
                throw new IllegalStateException("A policy document must have allowed or denied methods!");
            }
            return new Policy(this.constructPolicyDocument());
        }
    }

    private final String principalId;
    private final Map<String, Object> policyDocument;
    private final Map<String, Object> context;

    public static Builder builder() {
        return new Builder();
    }

    private Policy(final Builder builder) {
        this.principalId = builder.principalId;
        this.policyDocument = builder.policyDocument;
        this.context = builder.context;
    }

    @Override
    public String getPrincipalId() {
        return principalId;
    }

    @Override
    public Map<String, Object> getPolicyDocument() {
        return policyDocument;
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

    @Override
    public boolean equals(final Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof IPolicy)) {
            return false;
        }

        final IPolicy policy = (IPolicy) obj;
        return Objects.equals(principalId, policy.getPrincipalId())
                && Objects.equals(policyDocument, policy.getPolicyDocument())
                && Objects.equals(context, policy.getContext());
    }

    @Override
    public int hashCode() {
        return Objects.hash(principalId, policyDocument, context);
    }
}
