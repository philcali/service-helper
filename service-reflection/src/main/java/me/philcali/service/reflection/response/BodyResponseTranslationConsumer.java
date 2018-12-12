package me.philcali.service.reflection.response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import me.philcali.service.annotations.request.Body;
import me.philcali.service.annotations.response.Response;
import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.response.IResponse;
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
        if (result instanceof IResponse || Objects.isNull(result)) {
            return control;
        } else if (result instanceof String) {
            builder.withBody((String) result).withHeaders("Content-Type", "text/plain");
        } else {
            final Class<?> resultClass = result.getClass();
            try {
                String body = null;
                if (resultClass.getAnnotation(Response.class) != null || IOperation.class.isAssignableFrom(method.getDeclaringClass())) {
                    for (Field field : resultClass.getDeclaredFields()) {
                        if (field.getAnnotation(Body.class) != null) {
                            field.setAccessible(true);
                            body = marshaller.marshall(field.get(result));
                            break;
                        }
                    }
                } else {
                    body = marshaller.marshall(result);
                }
                Optional.ofNullable(body)
                        .ifPresent(builder.withHeaders("Content-Type", "application/json")::withBody);
            } catch (IOException
                    | IllegalAccessException
                    | IllegalArgumentException e) {
                throw new RuntimeException(e);
            }
        }
        return control;
    }

}
