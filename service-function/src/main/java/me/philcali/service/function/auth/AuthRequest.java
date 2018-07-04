package me.philcali.service.function.auth;

import java.util.Objects;

public class AuthRequest {
    private String authorizationToken;
    private String type;
    private String methodArn;

    public String getAuthorizationToken() {
        return authorizationToken;
    }

    public String getMethodArn() {
        return methodArn;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public void setAuthorizationToken(final String authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    public void setMethodArn(final String methodArn) {
        this.methodArn = methodArn;
    }

    @Override
    public boolean equals(final Object obj) {
        if (Objects.isNull(obj) || !(obj instanceof AuthRequest)) {
            return false;
        }

        final AuthRequest request = (AuthRequest) obj;
        return Objects.equals(authorizationToken, request.authorizationToken)
                && Objects.equals(methodArn, request.methodArn) && Objects.equals(type, request.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorizationToken, type, methodArn);
    }
}
