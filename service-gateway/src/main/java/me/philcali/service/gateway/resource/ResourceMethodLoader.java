package me.philcali.service.gateway.resource;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.system.SystemRequestRouterBuilder;

public class ResourceMethodLoader implements IResourceLoader {
    private final String jarFile;

    public ResourceMethodLoader(final String jarFile) {
        this.jarFile = jarFile;
    }

    @Override
    public Map<String, Map<String, ResourceMethod>> getMethods() {
        final List<ResourceMethod> methods = getResourceMethods();
        Collections.sort(methods, new ResourceMethodComparator());
        final Map<String, Map<String, ResourceMethod>> lookup = new LinkedHashMap<>();
        methods.forEach(method -> lookup.compute(method.getPatternPath(), (path, temp) -> {
            final Map<String, ResourceMethod> t = Optional.ofNullable(temp).orElseGet(HashMap::new);
            t.put(method.getMethod(), method);
            return t;
        }));
        return lookup;
    }

    private List<ResourceMethod> getResourceMethods() {
        return SystemRequestRouterBuilder.defaultRouter(jarFile).getResourceMethods();
    }
}
