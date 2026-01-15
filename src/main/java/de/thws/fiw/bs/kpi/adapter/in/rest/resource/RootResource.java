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

        String getAllProjects = linkService.buildCollectionLink(ProjectResource.class);
        String getAllKPIs = linkService.buildCollectionLink(KPIResource.class);

        return Response.ok()
                .header("Link", getAllProjects)
                .header("Link", getAllKPIs)
                .build();
    }
}
