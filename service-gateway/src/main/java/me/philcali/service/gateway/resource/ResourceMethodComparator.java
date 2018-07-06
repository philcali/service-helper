package me.philcali.service.gateway.resource;

import java.util.Comparator;

import me.philcali.service.binding.ResourceMethod;

public class ResourceMethodComparator implements Comparator<ResourceMethod> {
    @Override
    public int compare(final ResourceMethod methodA, final ResourceMethod methodB) {
        return methodA.getPatternPath().compareTo(methodB.getPatternPath());
    }
}
