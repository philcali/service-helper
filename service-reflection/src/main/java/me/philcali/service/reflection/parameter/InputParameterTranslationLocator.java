package me.philcali.service.reflection.parameter;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.function.Function;

import me.philcali.service.annotations.request.Body;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.exception.ResourceMethodCreationException;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;
import me.philcali.service.reflection.function.ITranslation;

public class InputParameterTranslationLocator<T extends AnnotatedElement> implements IAnnotatedElementTranslationLocator<T> {
    private final IObjectMarshaller marshaller;
    private final Function<T, Class<?>> lazyType;
    private final ITranslation<Field, IRequest> fieldTranslation;

    public InputParameterTranslationLocator(
            final IObjectMarshaller marshaller,
            final Function<T, Class<?>> lazyType,
            final ITranslation<Field, IRequest> fieldTranslation) {
        this.marshaller = marshaller;
        this.lazyType = lazyType;
        this.fieldTranslation = fieldTranslation;
    }

    @Override
    public Object apply(final T parameter, final IRequest request) {
        // TODO: Constructor translation locator
        Object result = null;
        try {
            final Class<?> theType = lazyType.apply(parameter);
            if (theType.getAnnotation(Body.class) != null) {
                result =  marshaller.unmarshall(request.getBody(), theType);
            } else {
                result = theType.newInstance();
                for (final Field field : theType.getDeclaredFields()) {
                    field.setAccessible(true);
                    final Object value = fieldTranslation.apply(field, request);
                    if (value != null) {
                        field.set(result, value);
                    }
                }
            }
        } catch (Exception e) {
            throw new ResourceMethodCreationException("Failed to create request type " + lazyType.apply(parameter), e);
        }
        return result;
    }

    @Override
    public boolean isApplicable(final T parameter) {
        return parameter.getAnnotations().length == 0;
    }

}
