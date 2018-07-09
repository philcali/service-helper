package me.philcali.service.reflection.system;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IModule;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class SystemRequestRouterBuilder {
    private List<String> jarFiles = new ArrayList<>();
    private ClassLoader loader;
    private IObjectMarshaller marshaller;

    public SystemRequestRouterBuilder withJarFiles(String ... jarFiles) {
        this.jarFiles.addAll(Arrays.asList(jarFiles));
        return this;
    }

    public SystemRequestRouterBuilder withLoader(final ClassLoader loader) {
        this.loader = loader;
        return this;
    }

    public SystemRequestRouterBuilder withMarshaller(final IObjectMarshaller marshaller) {
        this.marshaller = marshaller;
        return this;
    }

    public static RequestRouter defaultRouter(final String ... jarFiles) {
        return new SystemRequestRouterBuilder().withJarFiles(jarFiles).build();
    }

    public RequestRouter build() {
        final ClassLoader classLoader = new URLClassLoader(getModuleURLs(), Optional
                .ofNullable(loader)
                .orElseGet(ClassLoader::getSystemClassLoader));
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class, classLoader);
        final List<Object> components = StreamSupport.stream(moduleLoader.spliterator(), false)
                .flatMap(module -> module.getComponents().stream())
                .collect(Collectors.toList());
        return ReflectiveResourceRouter.builder()
                .withComponents(components)
                .withCollector(new DefaultResourceMethodCollector(Optional
                        .ofNullable(marshaller)
                        .orElseGet(ObjectMarshallerJackson::new)))
                .build();
    }

    private URL[] getModuleURLs() {
        try {
            final URL[] urls = new URL[jarFiles.size()];
            for (int index = 0; index < jarFiles.size(); index++) {
                urls[index] = new File(jarFiles.get(0)).toURI().toURL();
            }
            return urls;
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
