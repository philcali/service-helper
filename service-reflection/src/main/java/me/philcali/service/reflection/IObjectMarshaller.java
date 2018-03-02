package me.philcali.service.reflection;

import java.io.IOException;

public interface IObjectMarshaller {
    String marshall(Object obj) throws IOException;

    <T> T unmarshall(String content, Class<T> objectClass) throws IOException;
}
