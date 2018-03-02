package me.philcali.service.reflection.impl;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

import me.philcali.service.binding.IOperation;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.IReflectiveOperationFactory;
import me.philcali.service.reflection.ReflectiveOperation;
import me.philcali.service.reflection.function.ITranslation;
import me.philcali.service.reflection.response.IResponseTranslation;

public class DefaultReflectiveOperationFactory implements IReflectiveOperationFactory {
    public static class Builder {
        private ITranslation<Parameter, IRequest> parameters;
        private IResponseTranslation response;

        public DefaultReflectiveOperationFactory build() {
            return new DefaultReflectiveOperationFactory(this);
        }

        public Builder withParameterTranslation(final ITranslation<Parameter, IRequest> parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withResponseTranslation(final IResponseTranslation response) {
            this.response = response;
            return this;
        }
    }
    public static Builder builder() {
        return new Builder();
    }

    private final ITranslation<Parameter, IRequest> parameters;
    private final IResponseTranslation response;

    private DefaultReflectiveOperationFactory(final Builder builder) {
        this.parameters = builder.parameters;
        this.response = builder.response;
    }

    public DefaultReflectiveOperationFactory(final IObjectMarshaller marshaller) {
        this(builder()
                .withParameterTranslation(DefaultAnnotatedElementTranslation.parameters(marshaller))
                .withResponseTranslation(new DefaultResponseTranslation(marshaller)));
    }

    @Override
    public IOperation<IRequest, IResponse> create(final Object object, final Method method) {
        return new ReflectiveOperation(object, method, parameters, response);
    }

}
