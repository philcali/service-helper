package me.philcali.service.reflection.collector;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.IReflectiveOperationFactory;
import me.philcali.service.reflection.IResourceMethodLocator;

public class OperationResourceMethodCollector implements IResourceMethodCollector {
    private final IResourceMethodLocator locator;
    private final IReflectiveOperationFactory factory;

    public OperationResourceMethodCollector(
            final IResourceMethodLocator locator,
            final IReflectiveOperationFactory factory) {
        this.locator = locator;
        this.factory = factory;
    }

    @Override
    public Optional<List<ResourceMethod>> collect(final Object component, final Method method) {
        return locator.find(method)
                .filter(builder -> IOperation.class.isAssignableFrom(method.getReturnType()))
                .flatMap(builder -> {
                    final Object resource = createResource(component, method);
                    return findApply(resource)
                            .map(resourceMethod -> factory.create(resource, resourceMethod))
                            .map(builder::withOperation)
                            .map(ResourceMethod.Builder::build)
                            .map(Arrays::asList);
                });
    }

    private Optional<Method> findApply(final Object resource) {
        return Arrays.stream(resource.getClass().getMethods())
                .filter(method -> method.getName().equals("apply"))
                .findFirst();
    }
}
