package me.philcali.service.reflection.system;

import java.util.List;
import java.util.Objects;

public class CachingComponentProvider implements IComponentProvider {
    private final IComponentProvider componentProvider;
    private List<Object> components;

    public CachingComponentProvider(final IComponentProvider componentProvider) {
        this.componentProvider = componentProvider;
    }

    @Override
    public List<Object> getComponents() {
        if (Objects.isNull(components)) {
            components = componentProvider.getComponents();
        }
        return components;
    }
}
