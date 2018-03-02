package me.philcali.service.reflection.parameter;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;

public class RequestTranslationLocator<T extends AnnotatedElement> implements IAnnotatedElementTranslationLocator<T> {
    private final Function<T, Class<?>> lazyType;

    public RequestTranslationLocator(final Function<T, Class<?>> lazyType) {
        this.lazyType = lazyType;
    }

    @Override
    public Object apply(final T parameter, final IRequest request) {
        return request;
    }

    @Override
    public boolean isApplicable(final T parameter) {
        return lazyType.apply(parameter).isAssignableFrom(IRequest.class);
    }
}
