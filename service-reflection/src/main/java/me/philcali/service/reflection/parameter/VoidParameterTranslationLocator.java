package me.philcali.service.reflection.parameter;

import java.lang.reflect.AnnotatedElement;
import java.util.function.Function;

import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;

public class VoidParameterTranslationLocator<T extends AnnotatedElement> implements IAnnotatedElementTranslationLocator<T> {
    private final Function<T, Class<?>> lazyType;

    public VoidParameterTranslationLocator(final Function<T, Class<?>> lazyType) {
        this.lazyType = lazyType;
    }

    @Override
    public Object apply(final T parameter, final IRequest request) {
        return null;
    }

    @Override
    public boolean isApplicable(T parameter) {
        return lazyType.apply(parameter).equals(Void.class);
    }

}
