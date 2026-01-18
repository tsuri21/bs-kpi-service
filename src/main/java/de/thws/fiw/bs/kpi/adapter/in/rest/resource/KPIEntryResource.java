package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIEntryAPIMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.CreateKPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiEntry.KPIEntryDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntry;
import de.thws.fiw.bs.kpi.application.domain.model.kpiEntry.KPIEntryId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIEntryUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class KPIEntryResource {

    @Inject
    KPIEntryUseCase entryUseCase;

    @Inject
    KPIEntryAPIMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @Context
    UriInfo uriInfo;

    @GET
    @Path("{eId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @NotNull @PathParam("eId") UUID eId) {

        KPIEntryId id = new KPIEntryId(eId);
        KPIEntry entry = entryUseCase.readById(id).orElseThrow(NotFoundException::new);
        KPIEntryDTO dto = mapper.toApiModel(entry);

        URI selfUri = uriInfo.getAbsolutePath();
        linkService.setSelfLinkSub(dto, selfUri);
        Link self = linkService.buildSelfLinkSub(selfUri);
        Link delete = linkService.buildDeleteLinkSub(selfUri, KPIEntryResource.class);
        String allEntries = linkService.buildCollectionLinkSub(selfUri, KPIEntryResource.class);
        return Response.ok(dto)
                .links(self, delete)
                .header("Link", allEntries)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @PathParam("pId") UUID pId,
            @PathParam("aId") UUID aId,
            @QueryParam("from") String from,
            @QueryParam("to") String to,
            @Positive @DefaultValue("1") @QueryParam("page") int page) {

        Instant Ifrom = (from != null) ? Instant.parse(from) : null;
        Instant Ito = (to != null) ? Instant.parse(to) : null;
        final int PAGE_SIZE = 10;
        PageRequest request = new PageRequest(page, PAGE_SIZE);

        Page<KPIEntry> entryPage = entryUseCase.readAll(new KPIAssignmentId(aId), Ifrom, Ito, request);
        List<KPIEntryDTO> dtos = mapper.toApiModels(entryPage.content());

        URI selfUri = uriInfo.getAbsolutePath();
        linkService.setSelfLinksSub(dtos, selfUri);
        Link create = linkService.buildCreateLinkSub(selfUri, KPIEntry.class);
        Link[] pagination = linkService.buildPaginationLinks(entryPage);
        String assignment = linkService.buildSelfLinkSubLayerBack(selfUri, KPIAssignment.class);

        return Response.ok(dtos)
                .links(create)
                .links(pagination)
                .header("Link", assignment)
                .header("X-Total-Count", entryPage.totalElements())
                .build();
    }

    @POST
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
    public Response delete(
            @PathParam("aId") UUID aId,
            @PathParam("eId") UUID eId) {
        entryUseCase.delete(new KPIEntryId(eId));

        URI selfUri = uriInfo.getAbsolutePath();
        String allEntries = linkService.buildCollectionLinkSub(selfUri, KPIEntryResource.class);

        return Response.noContent()
                .header("Link", allEntries)
                .build();
    }


}
