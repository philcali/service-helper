package me.philcali.service.reflection.response;

import java.lang.reflect.Method;

import me.philcali.service.annotations.response.StatusCode;
import me.philcali.service.binding.response.Response.Builder;

public class StatusCodeTranslationConsumer implements IResponseTranslationConsumer {

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        StatusCode code = method.getAnnotation(StatusCode.class);
        if (code != null) {
            builder.withStatusCode(code.value());
        }
        return Control.CONTINUE;
    }

}
