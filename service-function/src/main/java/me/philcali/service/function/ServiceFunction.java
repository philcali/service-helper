package me.philcali.service.function;

import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.marshaller.jackson.ObjectMarshallerJackson;
import me.philcali.service.reflection.IModule;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class ServiceFunction implements RequestHandler<Request, IResponse> {
    // Saw a 10x improvement for statically caching this router
    private static RequestRouter CACHED_ROUTER;

    // Injection point for custom service functions
    protected IObjectMarshaller getObjectMarshaller() {
        return new ObjectMarshallerJackson();
    }

    protected RequestRouter getRequestRouter() {
        if (Objects.isNull(CACHED_ROUTER)) {
            final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class);
            final List<Object> components = StreamSupport.stream(moduleLoader.spliterator(), false)
                    .flatMap(module -> module.getComponents().stream())
                    .collect(Collectors.toList());
            CACHED_ROUTER = ReflectiveResourceRouter.builder()
                    .withComponents(components)
                    .withCollector(new DefaultResourceMethodCollector(getObjectMarshaller()))
                    .build();
        }
        return CACHED_ROUTER;
    }

    @Override
    public IResponse handleRequest(final Request input, final Context context) {
        return getRequestRouter().apply(input);
    }
}
