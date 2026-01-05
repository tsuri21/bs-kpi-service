package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIUseCase;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.annotations.cache.Cache;
import org.jboss.resteasy.annotations.cache.NoCache;

import java.util.List;
import java.util.UUID;

@Path("kpis")
public class KPIResource {

    @Inject
    KPIUseCase kpiUseCase;

    @Inject
    KPIApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @GET
    @Path("{id}")
    @Cache(maxAge = 60, isPrivate = true)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@NotNull @PathParam("id") UUID id) {
        KPIId kpiId = new KPIId(id);
        KPI kpi = kpiUseCase.readById(kpiId).orElseThrow(NotFoundException::new);
        KPIDTO kpiDTO = mapper.toApiModel(kpi);

        linkService.setSelfLink(kpiDTO);
        Link self = linkService.createSelfLink(id);
        Link delete = linkService.createDeleteLink(id);
        Link update = linkService.createUpdateLink(id);
        Link allProjects = linkService.createGetAllLink();

        return Response.ok(kpiDTO)
                .links(self, delete, update, allProjects)
                .build();
    }

    @GET
    @NoCache
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("name") Name name,
            @Positive @DefaultValue("1") @QueryParam("page") int page
    ) {
        final int PAGE_SIZE = 15;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<KPI> kpiPage = kpiUseCase.readAll(name, pageRequest);
        List<KPIDTO> kpis = mapper.toApiModels(kpiPage.content());

        linkService.setSelfLinks(kpis);
        Link create = linkService.createCreateLink();
        Link desc = linkService.createDescriptionLink();
        String search = linkService.createSearchTemplateLink("name");
        Link[] pagination = linkService.createPaginationLinks(kpiPage);

        return Response.ok(kpis)
                .links(create, desc)
                .links(pagination)
                .header("Link", search)
                .header("X-Total-Count", kpiPage.totalElements())
                .build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid KPIDTO kpiDto) {
        KPI kpi = mapper.toDomainModel(kpiDto);
        kpiUseCase.create(kpi);

        UUID newId = kpi.getId().value();
        Link self = linkService.createSelfLink(newId);
        Link allKPIs = linkService.createGetAllLink();

        return Response.created(linkService.createLocationUri(newId))
                .links(self, allKPIs)
                .build();
    }

    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(@NotNull @PathParam("id") UUID id, @Valid KPIDTO kpiDto) {
        if (!id.equals(kpiDto.getId())) {
            throw new IllegalArgumentException("The ID in the path does not match the ID in the body.");
        }

        KPI kpi = mapper.toDomainModel(kpiDto);
        kpiUseCase.update(kpi);

        return Response.noContent()
                .links(linkService.createSelfLink(id))
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@NotNull @PathParam("id") UUID id) {
        kpiUseCase.delete(new KPIId(id));

        return Response.noContent()
                .links(linkService.createGetAllLink())
                .build();
    }
}
