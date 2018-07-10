package me.philcali.service.reflection.parameter;

import java.lang.reflect.AnnotatedElement;
import java.util.Collections;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import me.philcali.service.annotations.request.CookieParam;
import me.philcali.service.binding.cookie.CookieDecoder;
import me.philcali.service.binding.cookie.ICookie;
import me.philcali.service.reflection.function.IPrimitiveTranslation;

public class CookieParamTranslationLocator<T extends AnnotatedElement> extends ParamTranslationLocator<T, CookieParam> {
    private static final CookieDecoder DEFAULT_COOKIE_DECODER = new CookieDecoder();

    public CookieParamTranslationLocator(
            final Function<T, String> defaultName,
            final Function<T, Class<?>> lazyType,
            final IPrimitiveTranslation primitives) {
        super(CookieParam.class,
                CookieParam::value,
                defaultName,
                lazyType,
                primitives,
                request -> Optional.ofNullable(request.getHeaders().get("cookie"))
                        .map(DEFAULT_COOKIE_DECODER::decode)
                        .map(cookies -> cookies.stream().collect(Collectors.toMap(
                                ICookie::getName,
                                ICookie::getValue)))
                        .orElseGet(Collections::emptyMap));
    }

}
