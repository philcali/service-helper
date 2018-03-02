package me.philcali.service.reflection.response;

import java.lang.reflect.Method;
import java.util.Objects;

import me.philcali.service.binding.response.Response.Builder;

public class NoContentResponseConsumer implements IResponseTranslationConsumer {

    @Override
    public Control accept(final Object result, final Method method, final Builder builder) {
        if (Objects.isNull(result)) {
            builder.withStatusCode(204);
            return Control.COMPLETE;
        }
        return Control.CONTINUE;
    }

}
