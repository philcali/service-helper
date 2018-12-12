package me.philcali.service.reflection.response;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import me.philcali.service.annotations.request.HeaderParam;
import me.philcali.service.annotations.response.Header;
import me.philcali.service.annotations.response.Headers;
import me.philcali.service.binding.response.Response.Builder;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.filter.IParamFilterMixin;
import me.philcali.service.reflection.filter.exception.MarshallingFilterException;

public class HeaderTranslationConsumer implements IResponseTranslationConsumer, IParamFilterMixin {
    private final IObjectMarshaller marshaller;

    public HeaderTranslationConsumer(final IObjectMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        final Headers headers = method.getAnnotation(Headers.class);
        if (Objects.nonNull(headers)) {
            pullFromHeadersAnnotation(headers, builder);
        } else if (Objects.nonNull(result)) {
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
                            final Function<String, String> thunk = composedFilters(field);
                            builder.withHeaders(Optional.ofNullable(annotation.value())
                                    .filter(name -> name.isEmpty())
                                    .orElseGet(field::getName), thunk.apply(getValue(value, field)));
                        } catch (IllegalArgumentException
                                | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private boolean isPrimitive(final Field field) {
        return field.getType().isPrimitive() || field.getType().equals(String.class);
    }

    private String getValue(final Object value, final Field field) {
        if (isPrimitive(field)) {
            return value.toString();
        } else if (Objects.isNull(value)) {
            return null;
        } else {
            try {
                return marshaller.marshall(value);
            } catch (IOException e) {
                throw new MarshallingFilterException(e);
            }
        }
    }
}
