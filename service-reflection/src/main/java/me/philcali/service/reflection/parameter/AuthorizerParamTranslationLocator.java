package me.philcali.service.reflection.parameter;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import me.philcali.service.annotations.request.AuthorizerParam;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;

public class AuthorizerParamTranslationLocator<T extends AnnotatedElement> implements IAnnotatedElementTranslationLocator<T> {
    
    @Override
    public Object apply(final T element, final IRequest request) {
        final AuthorizerParam param = element.getAnnotation(AuthorizerParam.class);
        final String key = param.value();
        return Optional.ofNullable(request.getRequestContext())
                .flatMap(context -> Optional.ofNullable(context.getAuthorizer()))
                .flatMap(authorizer -> Optional.ofNullable(authorizer.get(key)))
                .orElse(null);
    }
    
    @Override
    public boolean isApplicable(final T anything) {
        return anything.isAnnotationPresent(AuthorizerParam.class);
    }
}
