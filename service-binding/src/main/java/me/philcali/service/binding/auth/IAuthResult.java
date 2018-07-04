package me.philcali.service.binding.auth;

import java.util.Map;

public interface IAuthResult {
    String getPrincipalId();
    Map<String, Object> getContext();
    // TODO: allow API scopes to be built here
}
