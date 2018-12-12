package me.philcali.service.reflection.parameter.primitives;

import java.io.IOException;

import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.filter.exception.MarshallingFilterException;
import me.philcali.service.reflection.function.IPrimitiveTranslationLocator;

public class UnmarshalledTranslation implements IPrimitiveTranslationLocator {
    private final IObjectMarshaller marshaller;

    public UnmarshalledTranslation(final IObjectMarshaller marshaller) {
        this.marshaller = marshaller;
    }

    @Override
    public Object apply(final Class<?> inputClass, String json) {
        try {
            return marshaller.unmarshall(json, inputClass);
        } catch (IOException e) {
            throw new MarshallingFilterException(e);
        }
    }

    @Override
    public boolean isApplicable(final Class<?> inputClass) {
        return !inputClass.isPrimitive();
    }
}
