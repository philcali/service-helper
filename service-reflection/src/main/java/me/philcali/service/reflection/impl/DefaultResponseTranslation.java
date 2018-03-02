package me.philcali.service.reflection.impl;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.philcali.service.binding.response.IResponse;
import me.philcali.service.binding.response.Response;
import me.philcali.service.reflection.IObjectMarshaller;
import me.philcali.service.reflection.response.BodyResponseTranslationConsumer;
import me.philcali.service.reflection.response.HeaderTranslationConsumer;
import me.philcali.service.reflection.response.IResponseTranslation;
import me.philcali.service.reflection.response.IResponseTranslationConsumer;
import me.philcali.service.reflection.response.NoContentResponseConsumer;
import me.philcali.service.reflection.response.PassThroughTranslationConsumer;
import me.philcali.service.reflection.response.StatusCodeTranslationConsumer;

public class DefaultResponseTranslation implements IResponseTranslation {
    public static class Builder {
        private List<IResponseTranslationConsumer> consumers = new ArrayList<>();

        public DefaultResponseTranslation build() {
            return new DefaultResponseTranslation(this);
        }

        public Builder withConsumers(final IResponseTranslationConsumer...consumers) {
            return withConsumers(Arrays.asList(consumers));
        }

        public Builder withConsumers(final List<IResponseTranslationConsumer> consumers) {
            this.consumers.addAll(consumers);
            return this;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    private final List<IResponseTranslationConsumer> consumers;

    private DefaultResponseTranslation(final Builder builder) {
        this.consumers = builder.consumers;
    }

    public DefaultResponseTranslation(final IObjectMarshaller marshaller) {
        this(builder()
                .withConsumers(new NoContentResponseConsumer())
                .withConsumers(new PassThroughTranslationConsumer())
                .withConsumers(new HeaderTranslationConsumer())
                .withConsumers(new StatusCodeTranslationConsumer())
                .withConsumers(new BodyResponseTranslationConsumer(marshaller)));
    }

    @Override
    public IResponse translate(final Object result, final Method method) {
        final Response.Builder builder = Response.builder().withStatusCode(200);
        for (IResponseTranslationConsumer consumer : consumers) {
            switch (consumer.accept(result, method, builder)) {
            case COMPLETE:
                return builder.build();
            default:
                break;
            }
        }
        return builder.build();
    }

}
