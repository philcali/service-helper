package me.philcali.service.reflection;

import me.philcali.service.annotations.DELETE;
import me.philcali.service.annotations.POST;

public interface IOperationModule {
    @POST("/people")
    CreatePersonOperation createPerson();

    @DELETE("/person/{id}")
    DeletePersonOperation deletePerson();
}
