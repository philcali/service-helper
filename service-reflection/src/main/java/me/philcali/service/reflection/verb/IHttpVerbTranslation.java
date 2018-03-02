package me.philcali.service.reflection.verb;

import java.lang.annotation.Annotation;

import me.philcali.service.binding.ResourceMethod;

public interface IHttpVerbTranslation<T extends Annotation> {
    ResourceMethod.Builder translate(T annotation);
}
