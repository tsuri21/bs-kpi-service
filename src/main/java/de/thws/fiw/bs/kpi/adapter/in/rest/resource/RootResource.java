package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.cache.Cache;

@Path("/")
public class RootResource {

    @Inject
    HypermediaLinkService linkService;

    @GET
    @Cache(noStore = true)
    public Response showLinks() {

        Link getAllProjects = linkService.createGetAllLink(ProjectResource.class);
        Link getAllKPIs = linkService.createGetAllLink(KPIResource.class);

        return Response.ok()
                .links(getAllProjects, getAllKPIs)
                .build();
    }
}
