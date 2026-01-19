package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIAssignmentAPIMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIEvaluationResultApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIEvaluationResultDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.CreateKPIAssignmentDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.KPIAssignmentDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.EvaluationUseCase;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentUseCase;
import de.thws.fiw.bs.kpi.application.port.in.KPIUseCase;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;

@RequestScoped
public class KPIAssignmentResource {

    @Context
    UriInfo uriInfo;

    @Inject
    KPIAssignmentUseCase kpiAssignmentUseCase;

    @Inject
    KPIUseCase kpiUseCase;

    @Inject
    KPIAssignmentAPIMapper mapper;

    @Inject
    KPIApiMapper kpiMapper;

    @Inject
    EvaluationUseCase evaluationUseCase;

    @Inject
    KPIEvaluationResultApiMapper evaluationMapper;

    @Inject
    HypermediaLinkService linkService;

    @Context
    ResourceContext resourceContext;

    @Path("{aId}/entries")
    public KPIEntryResource entries(@PathParam("aId") UUID assignmentId) {
        return resourceContext.getResource(KPIEntryResource.class);
    }

    @GET
    @Path("{aId}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @PathParam("pId") UUID pId,
            @NotNull @PathParam("aId") UUID aId) {
        KPIAssignmentId assignmentId = new KPIAssignmentId(aId);
        KPIAssignment assignment = kpiAssignmentUseCase.readById(assignmentId).orElseThrow(NotFoundException::new);
        KPIAssignmentDTO assignmentDto = mapper.toApiModel(assignment);

        URI selfUri = uriInfo.getAbsolutePath();
        linkService.setSelfLinkSub(assignmentDto, selfUri);
        Link self = linkService.buildSelfLinkSub(selfUri);
        Link delete = linkService.buildDeleteLinkSub(selfUri, KPIAssignmentResource.class);
        Link update = linkService.buildUpdateLinkSub(selfUri, KPIAssignmentResource.class);
        String allKPIAssignments = linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class);
        URI entryUri = UriBuilder.fromUri(selfUri)
                .path("entries/1")
                .build();
        String allEntries = linkService.buildCollectionLinkSub(entryUri, KPIEntryResource.class, "fron", "to", "kpiId");
        Link evaluate = linkService.buildEvaluationLinkSub(selfUri, KPIAssignmentResource.class);
        return Response.ok(assignmentDto)
                .links(self, delete, update, evaluate)
                .header("Link", allKPIAssignments)
                .header("Link", allEntries)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("KPIId") UUID kpiId,
            @PathParam("pId") UUID pId,
            @Positive @DefaultValue("1") @QueryParam("page") int page) {
        final int PAGE_SIZE = 10;
        KPIId domainKPIId = (kpiId == null) ? null : new KPIId(kpiId);

        PageRequest request = new PageRequest(page, PAGE_SIZE);
        Page<KPIAssignment> assignmentPage = kpiAssignmentUseCase.readAll(domainKPIId, new ProjectId(pId), request);
        List<KPIAssignmentDTO> assignments = mapper.toApiModels(assignmentPage.content());

        URI selfUri = uriInfo.getAbsolutePath();
        URI kpiUri = UriBuilder.fromUri(selfUri)
                .path("availableKpis/1")
                .build();
        String availableKpisLink = linkService.buildCollectionLinkSub(kpiUri, KPIResource.class);
        linkService.setSelfLinksSub(assignments, selfUri);
        String project = linkService.buildSelfLinkSubLayerBack(selfUri, ProjectResource.class);
        Link create = linkService.buildCreateLinkSub(selfUri, KPIAssignmentResource.class);
        //Link desc = linkService.buildDescriptionLink();
        Link[] pagination = linkService.buildPaginationLinks(assignmentPage);

        return Response.ok(assignments)
                .links(pagination)
                .links(create)
                .header("Link", project)
                .header("Link", availableKpisLink)
                .header("X-Total-Count", assignmentPage.totalElements())
                .build();
    }

    @GET
    @Path("availableKpis")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableKpis(
            @PathParam("pId") UUID pId,
            @Positive @DefaultValue("1") @QueryParam("page") int page) {

        final int PAGE_SIZE = 10;

        PageRequest request = new PageRequest(page, PAGE_SIZE);
        Page<KPI> kpiPage = kpiUseCase.readAll(null, request);
        List<KPIDTO> kpis = kpiMapper.toApiModels(kpiPage.content());
        linkService.setSelfLinks(kpis, KPIResource.class);

        URI selfUri = uriInfo.getAbsolutePath();
        URI assignments = UriBuilder.fromUri(selfUri)
                .path("..")
                .build()
                .normalize();
        Link create = linkService.buildCreateLinkSub(assignments, KPIAssignmentResource.class);
        String allAssignments = linkService.buildCollectionLinkSub(assignments, KPIAssignment.class);
        Link[] pagination = linkService.buildPaginationLinks(kpiPage);

        return Response.ok(kpis)
                .links(create)
                .links(pagination)
                .header("Link", allAssignments)
                .header("X-Total-Count", kpiPage.totalElements())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(
            @PathParam("pId") UUID pId,
            @Valid CreateKPIAssignmentDTO assignmentDto) {

        KPIAssignmentId assignmentId = new KPIAssignmentId(UUID.randomUUID());
        KPIAssignmentCommand assignment = mapper.toDomainModelByCreate(assignmentDto, assignmentId, pId);
        kpiAssignmentUseCase.create(assignment);

        UUID newId = assignmentId.value();
        URI collectionUri = uriInfo.getAbsolutePath();

        return Response.created(linkService.buildLocationUriSub(collectionUri, newId))
                .links(linkService.buildSelfLinkSub(collectionUri, newId))
                .header("Link", linkService.buildCollectionLinkSub(collectionUri, newId, KPIAssignmentResource.class, "kpiId"))
                .build();
    }

    @PUT
    @Path("{aId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("pId") UUID pId,
            @PathParam("aId") UUID aId,
            @Valid CreateKPIAssignmentDTO updateDto) {

        KPIAssignmentId assignmentId = new KPIAssignmentId(aId);

        KPIAssignmentCommand command = mapper.toDomainModelByCreate(updateDto, assignmentId, pId);

        kpiAssignmentUseCase.update(command);

        URI selfUri = uriInfo.getAbsolutePath();

        return Response.noContent()
                .links(linkService.buildSelfLinkSub(selfUri))
                .header("Link", linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class, "kpiId"))
                .build();
    }

    @DELETE
    @Path("{aId}")
    public Response deleteById(
            @PathParam("pId") UUID pId,
            @NotNull @PathParam("aId") UUID aId) {
        kpiAssignmentUseCase.delete(new KPIAssignmentId(aId));

        URI selfUri = uriInfo.getAbsolutePath();
        return Response.noContent()
                .header("Link", linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class, "kpiId"))
                .build();
    }

    @GET
    @Path("{aId}/evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluation(@NotNull @PathParam("aId") UUID aId) {
        KPIEvaluationResult result = evaluationUseCase.evaluateKPI(new KPIAssignmentId(aId));
        KPIEvaluationResultDTO apiResult = evaluationMapper.toApiModel(result);

        URI selfUri = uriInfo.getAbsolutePath();
        String self = linkService.buildSelfLinkSubLayerBack(selfUri, KPIAssignmentResource.class);

        return Response.ok(apiResult)
                .header("Link", self)
                .build();
    }
}
