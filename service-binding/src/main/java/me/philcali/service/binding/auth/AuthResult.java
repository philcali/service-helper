package me.philcali.service.binding.auth;

import java.util.HashMap;
import java.util.Map;

public class AuthResult implements IAuthResult {
    private final String principalId;
    private final Map<String, Object> context;

    public static final class Builder {
        private String principalId;
        private Map<String, Object> context = new HashMap<>();

        public Builder withPrincipalId(final String principalId) {
            this.principalId = principalId;
            return this;
        }

        public Builder addContext(final String key, final Object value) {
            this.context.put(key, value);
            return this;
        }

        public IAuthResult build() {
            return new AuthResult(this);
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private AuthResult(final Builder builder) {
        this.principalId = builder.principalId;
        this.context = builder.context;
    }

    @Override
    public String getPrincipalId() {
        return principalId;
    }

    @Override
    public Map<String, Object> getContext() {
        return context;
    }

}
