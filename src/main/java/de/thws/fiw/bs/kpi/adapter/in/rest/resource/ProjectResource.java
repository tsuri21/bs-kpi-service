package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.ProjectApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.CreateProjectDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.project.ProjectDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.project.Project;
import de.thws.fiw.bs.kpi.application.domain.model.project.ProjectId;
import de.thws.fiw.bs.kpi.application.domain.model.project.RepoUrl;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.ProjectUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.annotations.cache.NoCache;

import java.util.List;
import java.util.UUID;

@Path("projects")
public class ProjectResource {

    @Inject
    ProjectUseCase projectUseCase;

    @Inject
    ProjectApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @GET
    @Path("{id}")
    @Cache(maxAge = 60, isPrivate = true)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById (@NotNull @PathParam("id") UUID id) {
        ProjectId projectId = new ProjectId(id);
        Project project = projectUseCase.readById(projectId).orElseThrow(NotFoundException::new);
        ProjectDTO projectDTO = mapper.toApiModel(project);

        linkService.setSelfLink(projectDTO);
        Link self = linkService.createSelfLink(id);
        Link delete = linkService.createDeleteLink(id);
        Link update = linkService.createUpdateLink(id);
        Link allProjects = linkService.createGetAllLink();

        return Response.ok(projectDTO)
                .links(self, delete, update, allProjects)
                .build();
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("name") Name name,
            @QueryParam("repoUrl") RepoUrl repoUrl,
            @Positive @DefaultValue("1") @QueryParam("page") int page
    ) {
        final int PAGE_SIZE = 10;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<Project> projectPage = projectUseCase.readAll(name, repoUrl, pageRequest);
        List<ProjectDTO> projects = mapper.toApiModels(projectPage.content());

        linkService.setSelfLinks(projects);
        Link create = linkService.createCreateLink();
        Link desc = linkService.createDescriptionLink();
        String search = linkService.createSearchTemplateLink("name", "repoUrl");
        Link[] pagination = linkService.createPaginationLinks(projectPage);

        return Response.ok(projects)
                .links(create, desc)
                .links(pagination)
                .header("Link", search)
                .header("X-Total-Count", projectPage.totalElements())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid CreateProjectDTO projectDto) {
        Project project = mapper.toDomainModelByCreate(projectDto);
        projectUseCase.create(project);

        UUID newId = project.getId().value();
        Link self = linkService.createSelfLink(newId);
        Link allProjects = linkService.createGetAllLink();

        return Response.created(linkService.createLocationUri(newId))
                .links(self, allProjects)
                .build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@NotNull @PathParam("id") UUID id, @Valid ProjectDTO projectDto) {
        if (!id.equals(projectDto.getId())) {
            throw new IllegalArgumentException("The ID in the path does not match the ID in the body.");
        }

        Project project = mapper.toDomainModel(projectDto);
        projectUseCase.update(project);

        return Response.noContent()
                .links(linkService.createSelfLink(id))
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@NotNull @PathParam("id") UUID id) {
        projectUseCase.delete(new ProjectId(id));

        return Response.noContent()
                .links(linkService.createGetAllLink())
                .build();
    }
}
