package me.philcali.service.binding.request;

import java.util.Map;

public interface IRequestContext {
    String getAccountId();
    String getApiId();
    Map<String, String> getAuthorizer();
    String getProtocol();
    String getRequestId();
    String getResourceId();
    String getStage();
}
