package me.philcali.service.reflection;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import me.philcali.service.binding.RequestRouter;
import me.philcali.service.binding.request.Request;
import me.philcali.service.binding.response.IResponse;
import me.philcali.service.reflection.impl.DefaultResourceMethodCollector;

public class ReflectiveResourceRouterTest {
    private ReflectiveResourceRouter.Builder builder;
    private IOperationModule personModule;
    private IObjectMarshaller marshaller;

    @Before
    public void setUp() {
        personModule = new PersonModule();
        builder = ReflectiveResourceRouter.builder()
                .withComponents(personModule)
                .withCollector(new DefaultResourceMethodCollector(marshaller));
    }


    @Test
    public void testCreatePerson() {
        final Person person = new Person();
        marshaller = new IObjectMarshaller() {
            @Override
            public String marshall(Object obj) throws IOException {
                return "tearing it down";
            }

            @Override
            public <T> T unmarshall(String content, Class<T> objectClass) throws IOException {
                assertEquals("did it", content);
                assertEquals(Person.class, objectClass);
                return (T) person;
            }
        };

        RequestRouter router = builder.withCollector(new DefaultResourceMethodCollector(marshaller)).build();
        Request request = new Request();
        request.setResource("/people");
        request.setHttpMethod("POST");
        request.setBody("did it");

        IResponse response = router.apply(request);
        assertEquals(person, personModule.createPerson().getPerson());
        assertEquals("tearing it down", response.getBody());
    }

    @Test
    public void testDeletePerson() throws Throwable {
        RequestRouter router = builder.build();
        Request request = new Request();
        request.setResource("/person/{id}");
        request.setHttpMethod("DELETE");
        Map<String, String> pathParameters = new HashMap<>();
        pathParameters.put("id", "abc-123");
        request.setPathParameters(pathParameters);
        IResponse response = router.apply(request);
        assertEquals(204, response.getStatusCode());
        assertEquals("abc-123", personModule.deletePerson().getPersonId());
    }

}
