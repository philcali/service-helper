package me.philcali.service.gateway;

import java.io.IOException;

import com.fasterxml.jackson.databind.ObjectMapper;

import me.philcali.service.reflection.IObjectMarshaller;

public class ObjectMarshallerJackson implements IObjectMarshaller {
    private final ObjectMapper mapper;

    public ObjectMarshallerJackson() {
        this(new ObjectMapper());
    }

    public ObjectMarshallerJackson(final ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public String marshall(final Object obj) throws IOException {
        return mapper.writeValueAsString(obj);
    }

    @Override
    public <T> T unmarshall(final String content, final Class<T> objectClass) throws IOException {
        return mapper.readValue(content, objectClass);
    }
}
