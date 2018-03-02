package me.philcali.service.reflection.parameter.primitives;

import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;

public class DoublePrimitiveTranslation implements IPrimitiveTranslationLocator {

    @Override
    public Object apply(final Class<?> thing, final String value) {
        return Double.parseDouble(value);
    }

    @Override
    public boolean isApplicable(final Class<?> anyClass) {
        return anyClass.isAssignableFrom(double.class);
    }
}
