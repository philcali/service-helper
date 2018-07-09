package me.philcali.service.function;

import java.util.Objects;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.system.SystemRequestRouterBuilder;

public class ServiceFunction implements RequestHandler<Request, IResponse> {
    // Saw a 10x improvement for statically caching this router
    private static RequestRouter CACHED_ROUTER;

    // Injection point for custom service functions
    protected IObjectMarshaller getObjectMarshaller() {
        return new ObjectMarshallerJackson();
    }

    protected RequestRouter getRequestRouter() {
        if (Objects.isNull(CACHED_ROUTER)) {
            CACHED_ROUTER = new SystemRequestRouterBuilder()
                    .withMarshaller(getObjectMarshaller())
                    .build();
        }
        return CACHED_ROUTER;
    }

    @Override
    public IResponse handleRequest(final Request input, final Context context) {
        return getRequestRouter().apply(input);
    }
}
