package me.philcali.service.reflection.collector;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.exception.ResourceMethodCreationException;

public interface IResourceMethodCollector {
    Optional<List<ResourceMethod>> collect(Object component, Method method);

    default Object createResource(final Object component, final Method method) {
        try {
            return method.invoke(component);
        } catch (IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ResourceMethodCreationException(e);
        }
    }
}
