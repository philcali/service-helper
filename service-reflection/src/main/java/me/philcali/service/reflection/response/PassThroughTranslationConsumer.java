package me.philcali.service.reflection.response;

import java.lang.reflect.Method;

import me.philcali.service.binding.response.IResponse;
import me.philcali.service.binding.response.Response.Builder;

public class PassThroughTranslationConsumer implements IResponseTranslationConsumer {

    @Override
    public Control accept(Object result, Method method, Builder builder) {
        if (result instanceof IResponse) {
            IResponse response = (IResponse) result;
            builder.withStatusCode(response.getStatusCode())
                    .withException(response.getException())
                    .withHeaders(response.getHeaders())
                    .withBody(response.getBody());
            return Control.COMPLETE;
        }
        return Control.CONTINUE;
    }

}
