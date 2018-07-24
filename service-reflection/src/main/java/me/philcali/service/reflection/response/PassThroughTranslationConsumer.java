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
                    .withRaw(response.isRaw())
                    .withBody(response.getBody());
            // Allow interpolation of annotated headers
            response.getHeaders().forEach(builder::withHeaders);
        }
        return Control.CONTINUE;
    }

}
