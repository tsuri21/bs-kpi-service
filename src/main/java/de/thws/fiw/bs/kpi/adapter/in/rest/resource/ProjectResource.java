package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.ProjectApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.ProjectEvaluationResultApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.CreateProjectDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectEvaluationResultDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.CachingUtil;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectEvaluationResult;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.EvaluationUseCase;
import de.thws.fiw.bs.kpi.application.port.in.ProjectUseCase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.container.ResourceContext;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@Path("projects")
@Authenticated
public class ProjectResource {

    @Inject
    ProjectUseCase projectUseCase;

    @Inject
    EvaluationUseCase evaluationUseCase;

    @Inject
    ProjectEvaluationResultApiMapper evaluationMapper;

    @Inject
    ProjectApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    CachingUtil cachingUtil;

    @Inject
    UserContext userContext;

    @Context
    ResourceContext resourceContext;

    @Inject
    UriInfo uriInfo;

    @Path("{pId}/assignments")
    public KPIAssignmentResource getAssignmentResource() {
        return resourceContext.getResource(KPIAssignmentResource.class);
    }

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @PathParam("id") UUID id,
            @Context Request request) {
        ProjectId projectId = new ProjectId(id);
        Project project = projectUseCase.readById(projectId).orElseThrow(NotFoundException::new);
        ProjectDTO projectDTO = mapper.toApiModel(project);

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, projectDTO);

        if (builder.build().getStatus() != Response.Status.OK.getStatusCode()) {
            return builder.build();
        }

        if (userContext.isAdmin()) {
            builder.links(linkService.buildDeleteLink(id));
            builder.links(linkService.buildUpdateLink(id));
        }

        linkService.setSelfLink(projectDTO);
        Link self = linkService.buildSelfLink(id);
        URI selfUri = uriInfo.getAbsolutePath();
        URI assignments = UriBuilder.fromUri(selfUri).path("assignments/1").build();
        String allAssignments = linkService.buildCollectionLinkSub(assignments, KPIAssignmentResource.class, "kpiId");
        String allProjects = linkService.buildCollectionLink("name", "repoUrl");
        Link evaluation = linkService.buildEvaluationLinkSub(selfUri, ProjectResource.class);

        return builder
                .links(self, evaluation)
                .header("Link", allProjects)
                .header("Link", allAssignments)
                .build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("name") Name name,
            @QueryParam("repoUrl") RepoUrl repoUrl,
            @Positive @DefaultValue("1") @QueryParam("page") int page,
            @Context Request request) {
        final int PAGE_SIZE = 10;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<Project> projectPage = projectUseCase.readAll(name, repoUrl, pageRequest);
        List<ProjectDTO> projects = mapper.toApiModels(projectPage.content());

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, projects);

        if (builder.build().getStatus() != Response.Status.OK.getStatusCode()) {
            return builder.build();
        }

        if (userContext.isAdmin()) {
            builder.links(linkService.buildCreateLink());
        }

        linkService.setSelfLinks(projects);
        Link root = linkService.buildDispatcherLink();
        // Link desc = linkService.buildDescriptionLink();
        Link[] pagination = linkService.buildPaginationLinks(projectPage);

        return builder
                .links(root)
                .links(pagination)
                .header("X-Total-Count", projectPage.totalElements())
                .build();
    }

    @POST
    @RolesAllowed(Role.ADMIN_ROLE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid CreateProjectDTO projectDto) {
        Project project = mapper.toDomainModelByCreate(projectDto);
        projectUseCase.create(project);

        UUID newId = project.getId().value();

        return Response.created(linkService.buildLocationUri(newId))
                .links(linkService.buildSelfLink(newId))
                .header("Link", linkService.buildCollectionLink("name", "repoUrl"))
                .build();
    }

    @PUT
    @Path("{id}")
    @RolesAllowed(Role.ADMIN_ROLE)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") UUID id,
            @Valid ProjectDTO projectDto,
            @Context Request request) {
        if (!id.equals(projectDto.getId())) {
            throw new IllegalArgumentException("The ID in the path does not match the ID in the body.");
        }

        Project currentProject = projectUseCase.readById(new ProjectId(id)).orElseThrow(NotFoundException::new);
        cachingUtil.checkPreconditions(request, mapper.toApiModel(currentProject));

        EntityTag newEtag = new EntityTag(Integer.toHexString(projectDto.hashCode()));

        Project project = mapper.toDomainModel(projectDto);
        projectUseCase.update(project);

        return Response.noContent()
                .tag(newEtag)
                .links(linkService.buildSelfLink(id))
                .build();
    }

    @DELETE
    @Path("{id}")
    @RolesAllowed(Role.ADMIN_ROLE)
    public Response delete(@PathParam("id") UUID id) {
        projectUseCase.delete(new ProjectId(id));

        return Response.noContent()
                .header("Link", linkService.buildCollectionLink("name", "repoUrl"))
                .build();
    }

    @GET
    @Path("{id}/evaluate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response evaluate(
            @PathParam("id") UUID id,
            @Context Request request) {
        ProjectEvaluationResult result = evaluationUseCase.evaluateProject(new ProjectId(id));
        ProjectEvaluationResultDTO apiResult = evaluationMapper.toApiModel(result);

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, apiResult);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            URI selfUri = uriInfo.getAbsolutePath();
            String self = linkService.buildSelfLinkSubLayerBack(selfUri, ProjectResource.class);

            return builder
                    .header("Link", self)
                    .build();
        }
        return builder.build();
    }
}
