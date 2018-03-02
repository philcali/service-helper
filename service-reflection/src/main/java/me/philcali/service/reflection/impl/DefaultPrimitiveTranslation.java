package me.philcali.service.reflection.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.philcali.service.reflection.function.AbstractTargetedTranslation;
import me.philcali.service.reflection.function.IPrimitiveTranslation;
import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;
import me.philcali.service.reflection.function.ITranslationLocator;
import me.philcali.service.reflection.parameter.primitives.DoublePrimitiveTranslation;
import me.philcali.service.reflection.parameter.primitives.IntegerPrimitiveTranslation;

public class DefaultPrimitiveTranslation extends AbstractTargetedTranslation<Class<?>, String, IPrimitiveTranslationLocator> implements IPrimitiveTranslation {
    public static class Builder {
        private List<ITranslationLocator<Class<?>, String>> translations = new ArrayList<>();

        public DefaultPrimitiveTranslation build() {
            return new DefaultPrimitiveTranslation(this);
        }

        public Builder withTranslations(final IPrimitiveTranslationLocator ... translations) {
            Arrays.stream(translations).forEach(this.translations::add);
            return this;
        }

        public Builder withTranslations(final List<IPrimitiveTranslationLocator> translations) {
            this.translations.addAll(translations);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public DefaultPrimitiveTranslation() {
        this(builder()
                .withTranslations(new IntegerPrimitiveTranslation())
                .withTranslations(new DoublePrimitiveTranslation()));
    }

    private DefaultPrimitiveTranslation(final Builder builder) {
        super(builder.translations);
    }

}
