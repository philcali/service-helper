package me.philcali.service.reflection.system;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class SystemRequestRouterBuilder {
    private List<String> jarFiles = new ArrayList<>();
    private IComponentProvider componentProvider;
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

    public SystemRequestRouterBuilder withComponentProvider(final IComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
        return this;
    }

    public static RequestRouter defaultRouter(final String ... jarFiles) {
        return new SystemRequestRouterBuilder().withJarFiles(jarFiles).build();
    }

    public RequestRouter build() {
        return ReflectiveResourceRouter.builder()
                .withComponents(Optional.ofNullable(componentProvider)
                        .orElseGet(() -> new DynamicModuleComponentProvider(jarFiles, loader))
                        .getComponents())
                .withCollector(new DefaultResourceMethodCollector(Optional
                        .ofNullable(marshaller)
                        .orElseGet(ObjectMarshallerJackson::new)))
                .build();
    }
}
