package me.philcali.service.gateway.resource;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IModule;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class ResourceMethodLoader {
    private final String jarFile;

    public ResourceMethodLoader(final String jarFile) {
        this.jarFile = jarFile;
    }

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
        final ClassLoader loader = new URLClassLoader(getModuleURLs(), ClassLoader.getSystemClassLoader());
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class, loader);
        final List<Object> components = StreamSupport.stream(moduleLoader.spliterator(), false)
                .flatMap(module -> module.getComponents().stream())
                .collect(Collectors.toList());
        final RequestRouter router = ReflectiveResourceRouter.builder()
                .withComponents(components)
                .withCollector(new DefaultResourceMethodCollector(new ObjectMarshallerJackson()))
                .build();
        return router.getResourceMethods();
    }

    private URL[] getModuleURLs() {
        try {
            return new URL[] { new File(jarFile).toURI().toURL() };
        } catch (MalformedURLException e) {
            throw new ResourceMethodLoaderException(e);
        }
    }
}
