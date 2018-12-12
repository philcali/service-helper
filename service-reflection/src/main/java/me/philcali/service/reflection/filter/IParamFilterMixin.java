package me.philcali.service.reflection.filter;

import java.lang.reflect.AnnotatedElement;
import java.util.Objects;
import java.util.function.Function;

import me.philcali.service.annotations.request.ParamFilter;
import me.philcali.service.reflection.filter.exception.MarshallingFilterException;

public interface IParamFilterMixin {
    default Function<String, String> composedFilters(final AnnotatedElement element) {
        final ParamFilter filter = element.getAnnotation(ParamFilter.class);
        Function<String, String> rval = Function.identity();
        if (Objects.isNull(filter)) {
            return rval;
        }
        for (final Class<? extends Function<String, String>> filterThunk : filter.value()) {
            try {
                final Function<String, String> actualThunk = filterThunk.newInstance();
                rval = rval.andThen(actualThunk);
            } catch (InstantiationException | IllegalAccessException e) {
                throw new MarshallingFilterException(e);
            }
        }
        return rval;
    }
}
