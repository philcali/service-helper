package me.philcali.service.reflection;

import me.philcali.service.annotations.request.Body;

public class CreatePersonRequest {
    @Body
    private Person person;

    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}
