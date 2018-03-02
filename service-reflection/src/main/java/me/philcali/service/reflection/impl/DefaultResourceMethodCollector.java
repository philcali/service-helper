package me.philcali.service.reflection.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.IReflectiveOperationFactory;
import me.philcali.service.reflection.IResourceMethodLocator;
import me.philcali.service.reflection.collector.IResourceMethodCollector;
import me.philcali.service.reflection.collector.OperationResourceMethodCollector;
import me.philcali.service.reflection.collector.ResourceMethodCollector;

public class DefaultResourceMethodCollector implements IResourceMethodCollector {
    public static class Builder {
        private List<IResourceMethodCollector> collectors = new ArrayList<>();

        public DefaultResourceMethodCollector build() {
            return new DefaultResourceMethodCollector(this);
        }

        public Builder withCollectors(IResourceMethodCollector ... collectors) {
            return withCollectors(Arrays.asList(collectors));
        }

        public Builder withCollectors(final List<IResourceMethodCollector> collectors) {
            this.collectors.addAll(collectors);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<IResourceMethodCollector> collectors;

    private DefaultResourceMethodCollector(final Builder builder) {
        this.collectors = builder.collectors;
    }

    public DefaultResourceMethodCollector(IObjectMarshaller marshaller) {
        this(new DefaultResourceMethodLocator(), new DefaultReflectiveOperationFactory(marshaller));
    }

    public DefaultResourceMethodCollector(
            final IResourceMethodLocator locator,
            final IReflectiveOperationFactory factory) {
        this(builder()
                .withCollectors(new ResourceMethodCollector(locator, factory))
                .withCollectors(new OperationResourceMethodCollector(locator, factory)));
    }

    @Override
    public Optional<List<ResourceMethod>> collect(final Object component, final Method method) {
        Optional<List<ResourceMethod>> methods = Optional.empty();
        for (IResourceMethodCollector collector : collectors) {
            methods = collector.collect(component, method);
            if (methods.isPresent()) {
                break;
            }
        }
        return methods;
    }
}
