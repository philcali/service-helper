package me.philcali.service.reflection.function;

import java.lang.reflect.AnnotatedElement;

import me.philcali.service.binding.request.IRequest;

public interface IAnnotatedElementTranslationLocator<T extends AnnotatedElement>
        extends IAnnotatedElementTranslationFunction<T>, ITranslationLocator<T, IRequest> {
}
