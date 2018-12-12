package me.philcali.service.annotations.request;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Validation {
    boolean required();
    String pattern() default "";
    int min() default 0;
    int max() default Integer.MAX_VALUE;
}
