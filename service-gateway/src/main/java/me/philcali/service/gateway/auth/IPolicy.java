package me.philcali.service.gateway.auth;

import java.util.Map;

public interface IPolicy {
    String getPrincipalId();

    Map<String, Object> getPolicyDocument();

    Map<String, Object> getContext();
}
