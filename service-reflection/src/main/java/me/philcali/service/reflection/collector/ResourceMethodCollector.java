package me.philcali.service.reflection.collector;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import me.philcali.service.annotations.Resource;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.IReflectiveOperationFactory;
import me.philcali.service.reflection.IResourceMethodLocator;

public class ResourceMethodCollector implements IResourceMethodCollector {
    private final IResourceMethodLocator locator;
    private final IReflectiveOperationFactory factory;

    public ResourceMethodCollector(
            final IResourceMethodLocator locator,
            final IReflectiveOperationFactory factory) {
        this.locator = locator;
        this.factory = factory;
    }

    @Override
    public Optional<List<ResourceMethod>> collect(final Object component, final Method method) {
        return Optional.ofNullable(method.getAnnotation(Resource.class))
                .map(resource -> createResource(component, method))
                .map(resource -> {
                    final List<ResourceMethod> methods = new ArrayList<>();
                    Arrays.stream(resource.getClass().getMethods())
                            .forEach(resourceMethod -> {
                                locator.find(resourceMethod)
                                        .map(builder -> builder.withOperation(factory.create(resource, resourceMethod)))
                                        .map(ResourceMethod.Builder::build)
                                        .ifPresent(methods::add);
                            });
                    return methods;
                });
    }

}
