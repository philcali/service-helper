package me.philcali.service.reflection.function;

import java.util.List;

public abstract class AbstractTargetedTranslation<T, U, F extends ITranslationLocator<T, U>> implements ITranslation<T, U> {
    private final List<ITranslationLocator<T, U>> locators;

    public AbstractTargetedTranslation(final List<ITranslationLocator<T, U>> locators) {
        this.locators = locators;
    }

    @Override
    public Object apply(final T thing, final U value) {
        for (ITranslationLocator<T, U> locator : locators) {
            if (locator.isApplicable(thing)) {
                return locator.apply(thing, value);
            }
        }
        return null;
    }

}
