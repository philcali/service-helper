package me.philcali.service.reflection;

import me.philcali.service.annotations.request.PathParam;

public class DeletePersonRequest {
    @PathParam("id")
    private String personId;

    public String getPersonId() {
        return personId;
    }

    public void setPersonId(String personId) {
        this.personId = personId;
    }
}
