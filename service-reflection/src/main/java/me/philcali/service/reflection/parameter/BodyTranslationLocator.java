package me.philcali.service.reflection.parameter;

import java.io.IOException;
import java.lang.reflect.AnnotatedElement;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import me.philcali.service.annotations.request.Body;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;

public class BodyTranslationLocator<T extends AnnotatedElement> implements IAnnotatedElementTranslationLocator<T> {
    private final IObjectMarshaller marshaller;
    private final Function<T, Class<?>> lazyType;

    public BodyTranslationLocator(
            final Function<T, Class<?>> lazyType,
            final IObjectMarshaller marshaller) {
        this.marshaller = marshaller;
        this.lazyType = lazyType;
    }

    @Override
    public Object apply(final T parameter, final IRequest request) {
        try {
            final Class<?> theType = lazyType.apply(parameter);
            if (theType.equals(String.class)) {
                return request.getBody();
            } else if (theType.isAssignableFrom(Map.class)) {
                return marshaller.unmarshall(request.getBody(), HashMap.class);
            } else {
                return marshaller.unmarshall(request.getBody(), theType);
            }
        } catch (IOException ie) {
            throw new RuntimeException(ie);
        }
    }

    @Override
    public boolean isApplicable(final T parameter) {
        return parameter.getAnnotation(Body.class) != null;
    }

}
