package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

@Path("/")
public class RootResource {

    @Inject
    HypermediaLinkService linkService;

    @GET
    public Response showLinks() {

        Link getAllProjects = linkService.createGetAllLink(ProjectResource.class);

        return Response.ok()
                .links(getAllProjects)
                .build();
    }
}
