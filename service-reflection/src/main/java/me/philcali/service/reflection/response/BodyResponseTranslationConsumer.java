package me.philcali.service.reflection.response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import me.philcali.service.annotations.request.Body;
import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.response.Response.Builder;
import me.philcali.service.reflection.IObjectMarshaller;

public class BodyResponseTranslationConsumer implements IResponseTranslationConsumer {
    private final IObjectMarshaller marshaller;

    public BodyResponseTranslationConsumer(final IObjectMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        Control control = Control.CONTINUE;
        if (result instanceof String) {
            builder.withBody((String) result);
        } else {
            final Class<?> resultClass = result.getClass();
            try {
                if (resultClass.getAnnotation(Body.class) != null || !IOperation.class.isAssignableFrom(method.getDeclaringClass())) {
                    builder.withBody(marshaller.marshall(result));
                } else {
                    for (Field field : resultClass.getDeclaredFields()) {
                        if (field.getAnnotation(Body.class) != null) {
                            field.setAccessible(true);
                            builder.withBody(marshaller.marshall(field.get(result)));
                        }
                    }
                }
            } catch (IOException
                    | IllegalAccessException
                    | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
        return control;
    }

}
