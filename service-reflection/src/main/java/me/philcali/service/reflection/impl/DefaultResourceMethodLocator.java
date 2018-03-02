package me.philcali.service.reflection.impl;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import me.philcali.service.annotations.DELETE;
import me.philcali.service.annotations.GET;
import me.philcali.service.annotations.HEAD;
import me.philcali.service.annotations.OPTIONS;
import me.philcali.service.annotations.PATCH;
import me.philcali.service.annotations.POST;
import me.philcali.service.annotations.PUT;
import me.philcali.service.binding.ResourceMethod;
import me.philcali.service.reflection.IResourceMethodLocator;
import me.philcali.service.reflection.verb.DeleteResourceMethodLocator;
import me.philcali.service.reflection.verb.GetResourceMethodLocator;
import me.philcali.service.reflection.verb.HeadResourceMethodLocator;
import me.philcali.service.reflection.verb.IHttpVerbTranslation;
import me.philcali.service.reflection.verb.OptionsResourceMethodLocator;
import me.philcali.service.reflection.verb.PatchResourceMethodLocator;
import me.philcali.service.reflection.verb.PostResourceMethodLocator;
import me.philcali.service.reflection.verb.PutResourceMethodLocator;

public class DefaultResourceMethodLocator implements IResourceMethodLocator {

    public static class Builder {
        private Map<Class<? extends Annotation>, IHttpVerbTranslation<Annotation>> locators = new ConcurrentHashMap<>();

        public DefaultResourceMethodLocator build() {
            return new DefaultResourceMethodLocator(this);
        }

        public <T extends Annotation> Builder withLocator(Class<T> type, IHttpVerbTranslation<T> locator) {
            this.locators.put(type, (IHttpVerbTranslation<Annotation>) locator);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final Map<Class<? extends Annotation>, IHttpVerbTranslation<Annotation>> locators;

    public DefaultResourceMethodLocator() {
        this(builder()
                .withLocator(HEAD.class, new HeadResourceMethodLocator())
                .withLocator(OPTIONS.class, new OptionsResourceMethodLocator())
                .withLocator(DELETE.class, new DeleteResourceMethodLocator())
                .withLocator(PATCH.class, new PatchResourceMethodLocator())
                .withLocator(GET.class, new GetResourceMethodLocator())
                .withLocator(PUT.class, new PutResourceMethodLocator())
                .withLocator(POST.class, new PostResourceMethodLocator()));
    }

    private  DefaultResourceMethodLocator(final Builder builder) {
        this.locators = builder.locators;
    }

    @Override
    public Optional<ResourceMethod.Builder> find(final Method method) {
        return Arrays.stream(method.getAnnotations())
                .filter(annotation -> locators.containsKey(annotation.annotationType()))
                .findFirst()
                .map(annotation -> locators.get(annotation.annotationType()).translate(annotation));
    }

}
