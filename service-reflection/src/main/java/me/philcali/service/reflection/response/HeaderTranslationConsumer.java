package me.philcali.service.reflection.response;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;

import me.philcali.service.annotations.request.HeaderParam;
import me.philcali.service.annotations.response.Header;
import me.philcali.service.annotations.response.Headers;
import me.philcali.service.binding.response.Response.Builder;

public class HeaderTranslationConsumer implements IResponseTranslationConsumer {

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        final Headers headers = method.getAnnotation(Headers.class);
        if (Objects.nonNull(headers)) {
            pullFromHeadersAnnotation(headers, builder);
        } else {
            pullFromResult(result, builder);
        }
        return Control.CONTINUE;
    }

    private void pullFromHeadersAnnotation(final Headers headers, final Builder builder) {
        for (Header header : headers.value()) {
            builder.withHeaders(header.name(), String.join("; ", header.value()));
        }
    }

    private void pullFromResult(final Object result, final Builder builder) {
        for (Field field : result.getClass().getDeclaredFields()) {
            Optional.ofNullable(field.getAnnotation(HeaderParam.class))
                    .ifPresent(annotation -> {
                        field.setAccessible(true);
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
    }
}
