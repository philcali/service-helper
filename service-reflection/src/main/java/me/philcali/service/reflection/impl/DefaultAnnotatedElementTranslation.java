package me.philcali.service.reflection.impl;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import me.philcali.service.annotations.request.FormParam;
import me.philcali.service.annotations.request.HeaderParam;
import me.philcali.service.annotations.request.PathParam;
import me.philcali.service.annotations.request.QueryParam;
import me.philcali.service.binding.request.IRequest;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.decoder.FormParamDecoder;
import me.philcali.service.reflection.decoder.IParamDecoder;
import me.philcali.service.reflection.function.AbstractTargetedTranslation;
import me.philcali.service.reflection.function.IAnnotatedElementTranslationLocator;
import me.philcali.service.reflection.function.IPrimitiveTranslation;
import me.philcali.service.reflection.function.ITranslation;
import me.philcali.service.reflection.function.ITranslationLocator;
import me.philcali.service.reflection.parameter.BodyTranslationLocator;
import me.philcali.service.reflection.parameter.InputParameterTranslationLocator;
import me.philcali.service.reflection.parameter.ParamTranslationLocator;
import me.philcali.service.reflection.parameter.RequestTranslationLocator;
import me.philcali.service.reflection.parameter.VoidParameterTranslationLocator;

public class DefaultAnnotatedElementTranslation<T extends AnnotatedElement> extends AbstractTargetedTranslation<T, IRequest, IAnnotatedElementTranslationLocator<T>> {
    public static class Builder<T extends AnnotatedElement> {
        private List<ITranslationLocator<T, IRequest>> locators = new ArrayList<>();

        public DefaultAnnotatedElementTranslation<T> build() {
            return new DefaultAnnotatedElementTranslation<>(this);
        }

        public Builder<T> withLocators(final IAnnotatedElementTranslationLocator<T> locators) {
            this.locators.add(locators);
            return this;
        }

        public Builder<T> withLocators(final List<IAnnotatedElementTranslationLocator<T>> locators) {
            this.locators.addAll(locators);
            return this;
        }
    }

    private static final IParamDecoder DEFAULT_FORM_DECODER = new FormParamDecoder();
    private static final IPrimitiveTranslation DEFAULT_PRIMITIVES = DefaultPrimitiveTranslation.builder().build();

    public static <T extends AnnotatedElement> Builder<T> builder() {
        return new Builder<>();
    }

    public static ITranslation<Field, IRequest> fields(final IObjectMarshaller marshaller) {
        return new DefaultAnnotatedElementTranslation<>(Field::getType, Field::getName, marshaller);
    }

    public static ITranslation<Parameter, IRequest> parameters(final IObjectMarshaller marshaller) {
        return new DefaultAnnotatedElementTranslation<>(Parameter::getType, Parameter::getName, marshaller);
    }

    private DefaultAnnotatedElementTranslation(final Builder<T> builder) {
        super(builder.locators);
    }

    public DefaultAnnotatedElementTranslation(
            final Function<T, Class<?>> lazyType,
            final Function<T, String> lazyName,
            final IObjectMarshaller marshaller) {
        this(DefaultAnnotatedElementTranslation.<T>builder()
                .withLocators(new VoidParameterTranslationLocator<>(lazyType))
                .withLocators(new RequestTranslationLocator<>(lazyType))
                .withLocators(new ParamTranslationLocator<T, QueryParam>(
                        QueryParam.class,
                        QueryParam::value,
                        lazyName,
                        lazyType,
                        DEFAULT_PRIMITIVES,
                        IRequest::getQueryStringParameters))
                .withLocators(new ParamTranslationLocator<T, PathParam>(
                        PathParam.class,
                        PathParam::value,
                        lazyName,
                        lazyType,
                        DEFAULT_PRIMITIVES,
                        IRequest::getPathParameters))
                .withLocators(new ParamTranslationLocator<T, HeaderParam>(
                        HeaderParam.class,
                        HeaderParam::value,
                        lazyName,
                        lazyType,
                        DEFAULT_PRIMITIVES,
                        IRequest::getHeaders))
                .withLocators(new ParamTranslationLocator<T, FormParam>(
                        FormParam.class,
                        FormParam::value,
                        lazyName,
                        lazyType,
                        DEFAULT_PRIMITIVES,
                        request -> DEFAULT_FORM_DECODER.decode(request.getBody())))
                .withLocators(new BodyTranslationLocator<>(lazyType, marshaller))
                .withLocators(new InputParameterTranslationLocator<>(marshaller, lazyType, fields(marshaller))));
    }

}
