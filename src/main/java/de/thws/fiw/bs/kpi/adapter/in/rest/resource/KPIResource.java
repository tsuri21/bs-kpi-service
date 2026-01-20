package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.KPIApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.CreateKPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.KPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.kpi.UpdateKPIDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.CachingUtil;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPI;
import de.thws.fiw.bs.kpi.application.domain.model.kpi.KPIId;
import de.thws.fiw.bs.kpi.application.domain.model.Name;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.KPIUseCase;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;

import java.util.List;
import java.util.UUID;

@Path("kpis")
@RolesAllowed(Role.ADMIN_ROLE)
public class KPIResource {

    @Inject
    KPIUseCase kpiUseCase;

    @Inject
    KPIApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    CachingUtil cachingUtil;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(
            @PathParam("id") UUID id,
            @Context Request request) {
        KPIId kpiId = new KPIId(id);
        KPI kpi = kpiUseCase.readById(kpiId).orElseThrow(NotFoundException::new);
        KPIDTO kpiDTO = mapper.toApiModel(kpi);

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, kpiDTO);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            linkService.setSelfLink(kpiDTO);
            Link self = linkService.buildSelfLink(id);
            Link delete = linkService.buildDeleteLink(id);
            Link update = linkService.buildPartialUpdateLink(id);
            String allKpis = linkService.buildCollectionLink("name");

            return builder
                    .links(self, delete, update)
                    .header("Link", allKpis)
                    .build();
        }
        return builder.build();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAll(
            @QueryParam("name") Name name,
            @Positive @DefaultValue("1") @QueryParam("page") int page,
            @Context Request request) {
        final int PAGE_SIZE = 15;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<KPI> kpiPage = kpiUseCase.readAll(name, pageRequest);
        List<KPIDTO> kpis = mapper.toApiModels(kpiPage.content());
        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, kpis);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            linkService.setSelfLinks(kpis);
            Link root = linkService.buildDispatcherLink();
            Link create = linkService.buildCreateLink();
            // Link desc = linkService.buildDescriptionLink();
            Link[] pagination = linkService.buildPaginationLinks(kpiPage);

            return builder
                    .links(root, create)
                    .links(pagination)
                    .header("X-Total-Count", kpiPage.totalElements())
                    .build();
        }
        return builder.build();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response create(@Valid CreateKPIDTO kpiDto) {
        KPI kpi = mapper.toDomainModelByCreate(kpiDto);
        kpiUseCase.create(kpi);

        UUID newId = kpi.getId().value();

        return Response.created(linkService.buildLocationUri(newId))
                .links(linkService.buildSelfLink(newId))
                .header("Link", linkService.buildCollectionLink("name"))
                .build();
    }

    @PATCH
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response update(
            @PathParam("id") UUID id,
            @Valid UpdateKPIDTO kpiDto,
            @Context Request request) {
        if (!id.equals(kpiDto.getId())) {
            throw new IllegalArgumentException("The ID in the path does not match the ID in the body.");
        }

        KPI currentKpi = kpiUseCase.readById(new KPIId(id)).orElseThrow(NotFoundException::new);
        cachingUtil.checkPreconditions(request, mapper.toApiModel(currentKpi));

        kpiUseCase.updateName(new KPIId(id), new Name(kpiDto.getName()));

        KPI newKpi = kpiUseCase.readById(new KPIId(id)).orElseThrow(NotFoundException::new);
        EntityTag newEtag = new EntityTag(Integer.toHexString(mapper.toApiModel(newKpi).hashCode()));

        return Response.noContent()
                .tag(newEtag)
                .links(linkService.buildSelfLink(id))
                .build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        kpiUseCase.delete(new KPIId(id));

        return Response.noContent()
                .header("Link", linkService.buildCollectionLink("name"))
                .build();
    }
}
