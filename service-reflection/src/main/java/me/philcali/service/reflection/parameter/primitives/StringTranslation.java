package me.philcali.service.reflection.parameter.primitives;

import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;

public class StringTranslation implements IPrimitiveTranslationLocator {

    @Override
    public Object apply(final Class<?> thing, final String value) {
        return value;
    }

    @Override
    public boolean isApplicable(final Class<?> thing) {
        return thing.equals(String.class);
    }

}
