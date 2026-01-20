package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.cache.Cache;

@Path("/")
@PermitAll
public class RootResource {

    @Inject
    HypermediaLinkService linkService;

    @Inject
    UserContext userContext;

    @GET
    @Cache(noStore = true)
    public Response showLinks() {

        Response.ResponseBuilder response = Response.ok();

        if (userContext.isAuthenticated()) {
            String getAllProjects = linkService.buildCollectionLink(ProjectResource.class);
            Link getCurrentUser = linkService.buildCustomLink(UserResource.class, "getCurrent", "getCurrentUser", "GET");

            response.links(getCurrentUser);
            response.header("Link", getAllProjects);

        } else {
            Link login = linkService.buildCustomLink(AuthenticationResource.class, "login", "login", "POST");
            Link register = linkService.buildCustomLink(UserResource.class, "register", "POST");
            response.links(login, register);
        }

        if (userContext.isAdmin()) {
            String getAllKPIs = linkService.buildCollectionLink(KPIResource.class);
            String getAllUsers = linkService.buildCollectionLink(UserResource.class);
            response.header("Link", getAllKPIs);
            response.header("Link", getAllUsers);
        }

        return response.build();
    }
}
