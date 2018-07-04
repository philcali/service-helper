package me.philcali.service.function.auth;

import java.util.Map;

public interface IPolicy {
    String getPrincipalId();

    Map<String, Object> getPolicyDocument();

    Map<String, Object> getContext();
}
