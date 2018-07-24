package me.philcali.service.binding.response;

import java.util.Map;

public interface IResponse {
    boolean isRaw();
    String getBody();
    Throwable getException();
    Map<String, String> getHeaders();
    int getStatusCode();
}
