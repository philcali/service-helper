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
import me.philcali.service.reflection.function.ITranslationLocator;
import me.philcali.service.reflection.parameter.AuthorizerParamTranslationLocator;
import me.philcali.service.reflection.parameter.BodyTranslationLocator;
import me.philcali.service.reflection.parameter.CookieParamTranslationLocator;
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
    private static final IPrimitiveTranslation DEFAULT_PRIMITIVES = DefaultPrimitiveTranslation.standard().build();

    public static <T extends AnnotatedElement> Builder<T> builder() {
        return new Builder<>();
    }

    public static Builder<Field> fields(final IObjectMarshaller marshaller) {
        return standard(Field::getType, Field::getName, marshaller);
    }

    public static Builder<Parameter> parameters(final IObjectMarshaller marshaller) {
        return standard(Parameter::getType, Parameter::getName, marshaller)
                .withLocators(new InputParameterTranslationLocator<>(marshaller, Parameter::getType, fields(marshaller).build()));
    }

    public static <T extends AnnotatedElement> Builder<T> standard(
            final Function<T, Class<?>> lazyType,
            final Function<T, String> lazyName,
            final IObjectMarshaller marshaller) {
        return DefaultAnnotatedElementTranslation.<T>builder()
                .withLocators(new VoidParameterTranslationLocator<T>(lazyType))
                .withLocators(new RequestTranslationLocator<T>(lazyType))
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
                .withLocators(new CookieParamTranslationLocator<T>(lazyName, lazyType, DEFAULT_PRIMITIVES))
                .withLocators(new AuthorizerParamTranslationLocator<T>())
                .withLocators(new BodyTranslationLocator<T>(lazyType, marshaller));
    }

    private DefaultAnnotatedElementTranslation(final Builder<T> builder) {
        super(builder.locators);
    }

}
