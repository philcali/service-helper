package me.philcali.service.gateway;

import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.reflection.IModule;
import me.philcali.service.reflection.ReflectiveResourceRouter;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class ServiceFunction implements RequestHandler<Request, IResponse> {
    @Override
    public IResponse handleRequest(final Request input, final Context context) {
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class);
        final List<Object> components = StreamSupport.stream(moduleLoader.spliterator(), false)
                .flatMap(module -> module.getComponents().stream())
                .collect(Collectors.toList());
        final RequestRouter router = ReflectiveResourceRouter.builder()
                .withComponents(components)
                .withCollector(new DefaultResourceMethodCollector(new ObjectMarshallerJackson()))
                .build();
        return router.apply(input);
    }
}
