package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIEntryApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.CreateKPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.KPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.CachingUtil;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIEntryUseCase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.cache.Cache;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestScoped
@Authenticated
public class KPIEntryResource {

    @Inject
    KPIEntryUseCase entryUseCase;

    @Inject
    KPIEntryApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    CachingUtil cachingUtil;

    @Inject
    UserContext userContext;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("{eId}")
    @Cache(maxAge = 3600)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @PathParam("eId") UUID eId) {

        KPIEntryId id = new KPIEntryId(eId);
        KPIEntry entry = entryUseCase.readById(id).orElseThrow(NotFoundException::new);
        KPIEntryDTO dto = mapper.toApiModel(entry);

        Response.ResponseBuilder builder = Response.ok(dto);

        URI selfUri = uriInfo.getAbsolutePath();

        if (userContext.isAdmin()) {
            builder.links(linkService.buildDeleteLinkSub(selfUri, KPIEntryResource.class));
        }

        linkService.setSelfLinkSub(dto, selfUri);
        Link self = linkService.buildSelfLinkSub(selfUri);
        String allEntries = linkService.buildCollectionLinkSub(selfUri, KPIEntryResource.class);

        return builder
                .links(self)
                .header("Link", allEntries)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @PathParam("aId") UUID aId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @Positive @DefaultValue("1") @QueryParam("page") int page,
            @Context Request request) {

        Instant Ifrom = (from != null) ? Instant.parse(from) : null;
        Instant Ito = (to != null) ? Instant.parse(to) : null;
        final int PAGE_SIZE = 10;
        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);

        Page<KPIEntry> entryPage = entryUseCase.readAll(new KPIAssignmentId(aId), Ifrom, Ito, pageRequest);
        List<KPIEntryDTO> dtos = mapper.toApiModels(entryPage.content());
        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, dtos);

        if (builder.build().getStatus() != Response.Status.OK.getStatusCode()) {
            return builder.build();
        }

        URI selfUri = uriInfo.getAbsolutePath();

        if (userContext.isAdmin()) {
            builder.links(linkService.buildCreateLinkSub(selfUri, KPIEntry.class));
        }

        linkService.setSelfLinksSub(dtos, selfUri);
        Link[] pagination = linkService.buildPaginationLinks(entryPage);
        String assignment = linkService.buildSelfLinkSubLayerBack(selfUri, KPIAssignment.class);

        return builder
                .links(pagination)
                .header("Link", assignment)
                .header("X-Total-Count", entryPage.totalElements())
                .build();
    }

    @POST
    @RolesAllowed({Role.ADMIN_ROLE, Role.TECH_USER_ROLE})
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("aId") UUID aId,
            @Valid CreateKPIEntryDTO kpiEntryDto) {

        KPIEntry kpiEntry = mapper.toDomainModelByCreate(kpiEntryDto, aId);
        entryUseCase.create(kpiEntry);

        UUID newId = kpiEntry.getId().value();

        URI collectionUri = uriInfo.getAbsolutePath();
        URI location = linkService.buildLocationUriSub(collectionUri, newId);
        Link self = linkService.buildSelfLinkSub(collectionUri, newId);
        String allEntries = linkService.buildCollectionLinkSub(collectionUri, newId, KPIEntryResource.class);

        return Response.created(location)
                .links(self)
                .header("Link", allEntries)
                .build();
    }

    @DELETE
    @Path("{eId}")
    @RolesAllowed({Role.ADMIN_ROLE, Role.TECH_USER_ROLE})
    public Response delete(
            @PathParam("eId") UUID eId) {
        entryUseCase.delete(new KPIEntryId(eId));

        URI selfUri = uriInfo.getAbsolutePath();
        String allEntries = linkService.buildCollectionLinkSub(selfUri, KPIEntryResource.class);

        return Response.noContent()
                .header("Link", allEntries)
                .build();
    }


}
