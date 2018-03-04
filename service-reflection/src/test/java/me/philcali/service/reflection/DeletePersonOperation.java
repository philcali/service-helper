package me.philcali.service.reflection;

import me.philcali.service.binding.IOperation;

public class DeletePersonOperation implements IOperation<DeletePersonRequest, Void> {
    private String personId;

    @Override
    public Void apply(final DeletePersonRequest input) {
        this.personId = input.getPersonId();
        return null;
    }

    public String getPersonId() {
        return personId;
    }
}
