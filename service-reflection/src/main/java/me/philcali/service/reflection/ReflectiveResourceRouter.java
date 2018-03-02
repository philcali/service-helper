package me.philcali.service.reflection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.reflection.collector.IResourceMethodCollector;

public class ReflectiveResourceRouter {
    public static class Builder {
        private List<Object> components = new ArrayList<>();
        private IResourceMethodCollector collector;

        public RequestRouter build() {
            Objects.requireNonNull(collector, "Resource collector is null!");
            final RequestRouter.Builder builder = RequestRouter.builder();
            for (Object component : components) {
                Class<?> componentClass = component.getClass();
                for (Class<?> componentInterface : componentClass.getInterfaces()) {
                     Arrays.stream(componentInterface.getMethods())
                             .map(method -> collector.collect(component, method))
                             .forEach(method -> method.ifPresent(builder::withResources));
                }
            }
            return builder.build();
        }

        public Builder withCollector(final IResourceMethodCollector collector) {
            this.collector = collector;
            return this;
        }

        public Builder withComponents(final List<Object> components) {
            this.components.addAll(components);
            return this;
        }

        public Builder withComponents(final Object ... components) {
            return withComponents(Arrays.asList(components));
        }

    }

}
