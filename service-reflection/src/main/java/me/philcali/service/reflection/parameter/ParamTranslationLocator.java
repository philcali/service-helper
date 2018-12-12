package me.philcali.service.reflection.parameter;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;

import me.philcali.service.annotations.request.DefaultValue;
import me.philcali.service.annotations.request.Validation;
import me.philcali.service.annotations.request.Validations;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.filter.IParamFilterMixin;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;
import me.philcali.service.reflection.function.IPrimitiveTranslation;
import me.philcali.service.reflection.parameter.exception.ValidationException;

public class ParamTranslationLocator<A extends AnnotatedElement, T extends Annotation>
        implements IAnnotatedElementTranslationLocator<A>, IParamFilterMixin {
    private final Class<T> targetParam;
    private final Function<T, String> value;
    private final Function<A, String> defaultName;
    private final Function<IRequest, Map<String, String>> pool;
    private final Function<A, Class<?>> lazyType;
    private IPrimitiveTranslation primitives;

    public ParamTranslationLocator(
            final Class<T> targetParam,
            final Function<T, String> value,
            final Function<A, String> defaultName,
            final Function<A, Class<?>> lazyType,
            final IPrimitiveTranslation primitives,
            final Function<IRequest, Map<String, String>> pool) {
        this.targetParam = targetParam;
        this.value = value;
        this.defaultName = defaultName;
        this.lazyType = lazyType;
        this.pool = pool;
        this.primitives = primitives;
    }

    @Override
    public Object apply(final A element, final IRequest request) {
        final Map<String, String> values = pool.apply(request);
        String key = value.apply(element.getAnnotation(targetParam));
        if (key.isEmpty()) {
            key = defaultName.apply(element);
        }
        final Optional<String> value = Optional.ofNullable(values.getOrDefault(key, getDefaultValue(element)));
        final String validated = validate(element, key, value.map(composedFilters(element)).orElse(null));
        if (Objects.nonNull(validated)) {
            return primitives.apply(lazyType.apply(element), validated);
        }
        return null;
    }

    @Override
    public boolean isApplicable(final A element) {
        return element.getAnnotation(targetParam) != null;
    }

    private String getDefaultValue(final A element) {
        return Optional.ofNullable(element.getAnnotation(DefaultValue.class))
                .map(DefaultValue::value)
                .orElse(null);
    }

    private String validate(final A element, final String key, final String value) {
        final Validations validations = element.getAnnotation(Validations.class);
        if (Objects.nonNull(validations)) {
            for (Validation validation : validations.value()) {
                if (Objects.isNull(value)) {
                    if (validation.required()) {
                        throw new ValidationException(key + " is required.");
                    }
                    break;
                }
                if (!validation.pattern().isEmpty() && Pattern.matches(validation.pattern(), value)) {
                    throw new ValidationException(key + " does not match " + validation.pattern());
                }
                if (value.length() < validation.max() || value.length() > validation.max()) {
                    throw new ValidationException(key + " size is not within " + validation.min() + " or " + validation.max());
                }
            }
        }
        return value;
    }
}
