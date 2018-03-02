package me.philcali.service.reflection.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.function.Function;

import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;
import me.philcali.service.reflection.function.IPrimitiveTranslation;

public class ParamTranslationLocator<A extends AnnotatedElement, T extends Annotation>
        implements IAnnotatedElementTranslationLocator<A> {
    private final Class<T> targetParam;
    private final Function<T, String> value;
    private final Function<A, String> defaultName;
    private final Function<IRequest, Map<String, String>> pool;
    private final Function<A, Class<?>> lazyType;
    private IPrimitiveTranslation primitives;

    public ParamTranslationLocator(
            final Class<T> targetParam,
            final Function<T, String> value,
            final Function<A, String> defaultName,
            final Function<A, Class<?>> lazyType,
            final IPrimitiveTranslation primitives,
            final Function<IRequest, Map<String, String>> pool) {
        this.targetParam = targetParam;
        this.value = value;
        this.defaultName = defaultName;
        this.lazyType = lazyType;
        this.pool = pool;
        this.primitives = primitives;
    }

    @Override
    public Object apply(final A element, final IRequest request) {
        final Map<String, String> values = pool.apply(request);
        String key = value.apply(element.getAnnotation(targetParam));
        if (key.isEmpty()) {
            key = defaultName.apply(element);
        }
        return primitives.apply(lazyType.apply(element), values.get(key));
    }

    @Override
    public boolean isApplicable(final A element) {
        return element.getAnnotation(targetParam) != null;
    }

}
