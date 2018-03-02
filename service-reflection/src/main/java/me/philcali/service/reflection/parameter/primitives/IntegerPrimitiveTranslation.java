package me.philcali.service.reflection.parameter.primitives;

import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;

public class IntegerPrimitiveTranslation implements IPrimitiveTranslationLocator {

    @Override
    public Object apply(Class<?> thingClass, String value) {
        return Integer.parseInt(value);
    }

    @Override
    public boolean isApplicable(Class<?> anyClass) {
        return anyClass.isAssignableFrom(int.class);
    }

}
