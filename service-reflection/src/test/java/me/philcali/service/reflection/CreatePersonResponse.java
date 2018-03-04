package me.philcali.service.reflection;

import me.philcali.service.annotations.request.Body;

@Body
public class CreatePersonResponse {
    private final String personId;

    public CreatePersonResponse(final String personId) {
        this.personId = personId;
    }

    public String getPersonId() {
        return personId;
    }
}
