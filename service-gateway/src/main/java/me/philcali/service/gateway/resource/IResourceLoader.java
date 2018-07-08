package me.philcali.service.gateway.resource;

import java.util.Map;

import me.philcali.service.binding.ResourceMethod;

public interface IResourceLoader {
    Map<String, Map<String, ResourceMethod>> getMethods();
}
