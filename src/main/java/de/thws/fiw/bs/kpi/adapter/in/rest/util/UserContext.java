package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.SecurityContext;


@RequestScoped
public class UserContext {

    @Inject
    SecurityContext securityContext;

    public boolean isAdmin() {
        return securityContext.isUserInRole(Role.ADMIN_ROLE);
    }

    public boolean isTechnicalUser() {
        return securityContext.isUserInRole(Role.TECH_USER_ROLE);
    }

    public boolean isMember() {
        return securityContext.isUserInRole(Role.MEMBER_ROLE);
    }
}
