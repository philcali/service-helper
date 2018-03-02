package me.philcali.service.reflection.response;

import java.lang.reflect.Method;

import me.philcali.service.binding.response.Response;

public interface IResponseTranslationConsumer {
    enum Control {
        CONTINUE,
        COMPLETE;
    }

    Control accept(Object result, Method method, Response.Builder builder);
}
