package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIAssignmentApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIEvaluationResultApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIEvaluationResultDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.CreateKPIAssignmentDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpiAssignment.KPIAssignmentDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.CachingUtil;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignment;
import de.thws.fiw.bs.kpi.application.domain.model.kpiAssignment.KPIAssignmentId;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.EvaluationUseCase;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentCommand;
import de.thws.fiw.bs.kpi.application.port.in.KPIAssignmentUseCase;
import de.thws.fiw.bs.kpi.application.port.in.KPIUseCase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.*;

@RequestScoped
@Authenticated
public class KPIAssignmentResource {

    @Context
    UriInfo uriInfo;

    @Inject
    KPIAssignmentUseCase kpiAssignmentUseCase;

    @Inject
    KPIUseCase kpiUseCase;

    @Inject
    KPIAssignmentApiMapper mapper;

    @Inject
    KPIApiMapper kpiMapper;

    @Inject
    EvaluationUseCase evaluationUseCase;

    @Inject
    KPIEvaluationResultApiMapper evaluationMapper;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    CachingUtil cachingUtil;

    @Inject
    UserContext userContext;

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
            @PathParam("aId") UUID aId,
            @Context Request request) {
        KPIAssignmentId assignmentId = new KPIAssignmentId(aId);
        KPIAssignment assignment = kpiAssignmentUseCase.readById(assignmentId).orElseThrow(NotFoundException::new);
        KPIAssignmentDTO assignmentDto = mapper.toApiModel(assignment);
        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, assignmentDto);

        if (builder.build().getStatus() != Response.Status.OK.getStatusCode()) {
            return builder.build();
        }

        URI selfUri = uriInfo.getAbsolutePath();
        URI entryUri = UriBuilder.fromUri(selfUri)
                .path("entries/1")
                .build();

        if (userContext.isAdmin()) {
            builder.links(linkService.buildDeleteLinkSub(selfUri, KPIAssignmentResource.class));
            builder.links(linkService.buildUpdateLinkSub(selfUri, KPIAssignmentResource.class));
        }

        linkService.setSelfLinkSub(assignmentDto, selfUri);
        Link self = linkService.buildSelfLinkSub(selfUri);
        String allKPIAssignments = linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class);
        String allEntries = linkService.buildCollectionLinkSub(entryUri, KPIEntryResource.class, "fron", "to", "kpiId");
        Link evaluate = linkService.buildEvaluationLinkSub(selfUri, KPIAssignmentResource.class);

        return builder
                .links(self, evaluate)
                .header("Link", allKPIAssignments)
                .header("Link", allEntries)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("KPIId") UUID kpiId,
            @PathParam("pId") UUID pId,
            @Positive @DefaultValue("1") @QueryParam("page") int page,
            @Context Request request) {
        final int PAGE_SIZE = 10;
        KPIId domainKPIId = (kpiId == null) ? null : new KPIId(kpiId);

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<KPIAssignment> assignmentPage = kpiAssignmentUseCase.readAll(domainKPIId, new ProjectId(pId), pageRequest);
        List<KPIAssignmentDTO> assignments = mapper.toApiModels(assignmentPage.content());

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, assignments);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            return builder.build();
        }

        URI selfUri = uriInfo.getAbsolutePath();
        URI kpiUri = UriBuilder.fromUri(selfUri)
                .path("availableKpis/1")
                .build();

        if (userContext.isAdmin()) {
            builder.links(linkService.buildCreateLinkSub(selfUri, KPIAssignmentResource.class));
        }

        linkService.setSelfLinksSub(assignments, selfUri);
        String availableKpisLink = linkService.buildCollectionLinkSub(kpiUri, KPIResource.class);
        String project = linkService.buildSelfLinkSubLayerBack(selfUri, ProjectResource.class);
        Link[] pagination = linkService.buildPaginationLinks(assignmentPage);

        return builder
                .links(pagination)
                .header("Link", project)
                .header("Link", availableKpisLink)
                .header("X-Total-Count", assignmentPage.totalElements())
                .build();
    }

    @GET
    @Path("availableKpis")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAvailableKpis(
            @Positive @DefaultValue("1") @QueryParam("page") int page,
            @Context Request request) {

        final int PAGE_SIZE = 10;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<KPI> kpiPage = kpiUseCase.readAll(null, pageRequest);
        List<KPIDTO> kpis = kpiMapper.toApiModels(kpiPage.content());

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, kpis);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            linkService.setSelfLinks(kpis, KPIResource.class);
            URI selfUri = uriInfo.getAbsolutePath();
            URI assignments = UriBuilder.fromUri(selfUri)
                    .path("..")
                    .build()
                    .normalize();
            Link create = linkService.buildCreateLinkSub(assignments, KPIAssignmentResource.class);
            String allAssignments = linkService.buildCollectionLinkSub(assignments, KPIAssignment.class);
            Link[] pagination = linkService.buildPaginationLinks(kpiPage);

            return builder
                    .links(create)
                    .links(pagination)
                    .header("Link", allAssignments)
                    .header("X-Total-Count", kpiPage.totalElements())
                    .build();
        }
        return builder.build();
    }

    @POST
    @RolesAllowed(Role.ADMIN_ROLE)
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
    @RolesAllowed(Role.ADMIN_ROLE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("aId") UUID aId,
            @Valid KPIAssignmentDTO updateDto,
            @Context Request request) {

        if (!aId.equals(updateDto.getId())) {
            throw new IllegalArgumentException("The ID in the path does not match the ID in the body.");
        }

        KPIAssignmentCommand command = mapper.toDomainModel(updateDto);

        KPIAssignment currentAssignment = kpiAssignmentUseCase.readById(new KPIAssignmentId(aId)).orElseThrow(NotFoundException::new);
        cachingUtil.checkPreconditions(request, mapper.toApiModel(currentAssignment));

        EntityTag newEtag = new EntityTag(Integer.toHexString(updateDto.hashCode()));

        kpiAssignmentUseCase.update(command);
        URI selfUri = uriInfo.getAbsolutePath();

        return Response.noContent()
                .tag(newEtag)
                .links(linkService.buildSelfLinkSub(selfUri))
                .header("Link", linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class, "kpiId"))
                .build();
    }

    @DELETE
    @Path("{aId}")
    @RolesAllowed(Role.ADMIN_ROLE)
    public Response delete(
            @PathParam("aId") UUID aId) {
        kpiAssignmentUseCase.delete(new KPIAssignmentId(aId));

        URI selfUri = uriInfo.getAbsolutePath();
        return Response.noContent()
                .header("Link", linkService.buildCollectionLinkSub(selfUri, KPIAssignmentResource.class, "kpiId"))
                .build();
    }

    @GET
    @Path("{aId}/evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEvaluation(
            @PathParam("aId") UUID aId,
            @Context Request request) {
        KPIEvaluationResult result = evaluationUseCase.evaluateKPI(new KPIAssignmentId(aId));
        KPIEvaluationResultDTO apiResult = evaluationMapper.toApiModel(result);

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, apiResult);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            URI selfUri = uriInfo.getAbsolutePath();
            String self = linkService.buildSelfLinkSubLayerBack(selfUri, KPIAssignmentResource.class);

            return builder
                    .header("Link", self)
                    .build();
        }
        return builder.build();
    }
}
