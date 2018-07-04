package me.philcali.service.gateway.auth;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import me.philcali.service.annotations.request.Authorizer;
import me.philcali.service.annotations.request.TokenFilter;
import me.philcali.service.binding.auth.IAuthResult;
import me.philcali.service.binding.response.UnauthorizedException;
import me.philcali.service.reflection.IModule;

public class CustomAuthorizerFunction implements RequestHandler<AuthRequest, IPolicy> {

    @Override
    public IPolicy handleRequest(final AuthRequest input, final Context context) {
        final ServiceLoader<IModule> moduleLoader = ServiceLoader.load(IModule.class);
        final Optional<IAuthResult> authResult = StreamSupport.stream(moduleLoader.spliterator(), false)
                .flatMap(module -> module.getComponents().stream())
                .map(component -> {
                    final Class<?> componentClass = component.getClass();
                    return Arrays.stream(componentClass.getMethods())
                            .filter(method -> isFunctionAuthorizer(context, method))
                            .findFirst()
                            .map(method -> invokeAuthorizer(method, component))
                            .map(operation -> operation.apply(input.getAuthorizationToken()));
                })
                .filter(Optional::isPresent)
                .findFirst()
                .map(Optional::get);
        return authResult.map(result -> Policy.builder()
                .withPrincipalId(result.getPrincipalId())
                .withContext(result.getContext())
                .withMethodArn(input.getMethodArn())
                .allowAllMethods()
                .build())
                .orElseThrow(UnauthorizedException::new);
    }

    private Function<String, IAuthResult> invokeAuthorizer(final Method method, final Object component) {
        try {
            final Function<String, IAuthResult> thunk = (Function<String, IAuthResult>) method.invoke(component);
            final TokenFilter filter = method.getAnnotation(TokenFilter.class);
            if (Objects.nonNull(filter)) {
                return thunk.compose(filter.value().newInstance());
            }
            return thunk;
        } catch (IllegalAccessException
                | IllegalArgumentException
                | InvocationTargetException
                | InstantiationException e) {
            throw new UnauthorizedException();
        }
    }

    private boolean isFunctionAuthorizer(final Context context, final Method method) {
        return Objects.nonNull(method.getAnnotation(Authorizer.class))
                && method.getReturnType().getSimpleName().equals(context.getFunctionName());
    }
}
