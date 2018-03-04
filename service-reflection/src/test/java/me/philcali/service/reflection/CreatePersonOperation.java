package me.philcali.service.reflection;

import me.philcali.service.binding.IOperation;

public class CreatePersonOperation implements IOperation<CreatePersonRequest, CreatePersonResponse> {
    private final String personId;
    private Person person;

    public CreatePersonOperation(final String personId) {
        this.personId = personId;
    }

    @Override
    public CreatePersonResponse apply(final CreatePersonRequest input) {
        this.person = input.getPerson();
        return new CreatePersonResponse(personId);
    }

    public Person getPerson() {
        return person;
    }
}
