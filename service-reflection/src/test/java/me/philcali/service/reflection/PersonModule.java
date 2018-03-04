package me.philcali.service.reflection;

import java.util.UUID;

public class PersonModule implements IOperationModule {
    private final CreatePersonOperation create = new CreatePersonOperation(UUID.randomUUID().toString());
    private final DeletePersonOperation delete = new DeletePersonOperation();

    @Override
    public CreatePersonOperation createPerson() {
        return create;
    }

    @Override
    public DeletePersonOperation deletePerson() {
        return delete;
    }
}
