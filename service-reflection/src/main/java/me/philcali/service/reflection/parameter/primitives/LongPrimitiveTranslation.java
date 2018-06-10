package me.philcali.service.reflection.parameter.primitives;

import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;

public class LongPrimitiveTranslation implements IPrimitiveTranslationLocator {
    
    @Override
    public Object apply(final Class<?> anyClass, final String content) {
        return Long.parseLong(content);
    }
    
    @Override
    public boolean isApplicable(final Class<?> thing) {
        return thing.isAssignableFrom(long.class);
    }
}
