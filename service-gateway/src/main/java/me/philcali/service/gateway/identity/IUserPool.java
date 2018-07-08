package me.philcali.service.gateway.identity;

import com.amazonaws.services.identitymanagement.model.Role;

public interface IUserPool {
    Role getRole(String roleName);
}
