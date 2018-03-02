package me.philcali.service.reflection;

import java.lang.reflect.Method;
import java.util.Optional;

import me.philcali.service.binding.ResourceMethod;

public interface IResourceMethodLocator {
    Optional<ResourceMethod.Builder> find(Method method);
}
