package me.philcali.service.reflection.response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Optional;

import me.philcali.service.annotations.request.HeaderParam;
import me.philcali.service.binding.response.Response.Builder;

public class HeaderTranslationConsumer implements IResponseTranslationConsumer {

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        for (Field field : result.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Optional.ofNullable(field.getAnnotation(HeaderParam.class))
                    .ifPresent(annotation -> {
                        try {
                            final Object value = field.get(result);
                            builder.withHeaders(Optional.ofNullable(annotation.value())
                                    .filter(name -> name.isEmpty())
                                    .orElseGet(field::getName), value.toString());
                        } catch (IllegalArgumentException
                                | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
        return Control.CONTINUE;
    }
}
