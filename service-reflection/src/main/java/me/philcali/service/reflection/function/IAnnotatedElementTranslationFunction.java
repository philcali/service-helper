package me.philcali.service.reflection.function;

import java.lang.reflect.AnnotatedElement;

import me.philcali.service.binding.request.IRequest;

public interface IAnnotatedElementTranslationFunction<A extends AnnotatedElement>
        extends ITranslation<A, IRequest> {
    @Override
    Object apply(A element, IRequest request);
}
