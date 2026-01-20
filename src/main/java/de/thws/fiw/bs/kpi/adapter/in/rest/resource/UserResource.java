package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.UserApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.user.UserResponseDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.CachingUtil;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import de.thws.fiw.bs.kpi.application.domain.model.user.Role;
import de.thws.fiw.bs.kpi.application.domain.model.user.User;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import de.thws.fiw.bs.kpi.application.port.in.UserManagementUseCase;
import io.quarkus.security.Authenticated;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.annotations.cache.Cache;

import java.util.List;
import java.util.UUID;

@Path("users")
@Authenticated
public class UserResource {

    @Inject
    UserManagementUseCase userManagementUseCase;

    @Inject
    JsonWebToken jwt;

    @Inject
    UserContext userContext;

    @Inject
    UserApiMapper mapper;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    CachingUtil cachingUtil;

    @GET
    @Path("me")
    @Cache(maxAge = 3600)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCurrent() {
        String id = jwt.getSubject();
        System.out.println(id);
        UUID uuid = UUID.fromString(id);

        return createGetResponse(uuid);
    }

    @GET
    @Path("{id}")
    @Cache(maxAge = 3600)
    @Produces(MediaType.APPLICATION_JSON)
    public Response getById(@NotNull @PathParam("id") UUID id) {
        checkAuthorization(id);

        return createGetResponse(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @RolesAllowed({Role.ADMIN_ROLE})
    public Response getAll(@Positive @DefaultValue("1") @QueryParam("page") int page,
                           @Context Request request) {
        final int PAGE_SIZE = 10;

        PageRequest pageRequest = new PageRequest(page, PAGE_SIZE);
        Page<User> userPage = userManagementUseCase.readAll(pageRequest);
        List<UserResponseDTO> users = mapper.toApiModels(userPage.content());

        Response.ResponseBuilder builder = cachingUtil.getConditionalBuilder(request, users);

        if (builder.build().getStatus() == Response.Status.OK.getStatusCode()) {
            linkService.setSelfLinks(users);
            Link[] pagination = linkService.buildPaginationLinks(userPage);
            Link root = linkService.buildDispatcherLink();

            return builder
                    .links(root)
                    .links(pagination)
                    .header("X-Total-Count", userPage.totalElements())
                    .build();
        }
        return builder.build();
    }

    @DELETE
    @Path("{id}")
    public Response delete(@PathParam("id") UUID id) {
        checkAuthorization(id);

        userManagementUseCase.delete(new UserId(id));

        Link root = linkService.buildDispatcherLink();

        Response.ResponseBuilder response = Response.noContent().links(root);

        if (userContext.isAdmin()) {
            response.header("Link", linkService.buildCollectionLink());
        }

        return response.build();
    }

    private void checkAuthorization(UUID resourceId) {
        String currentUserId = jwt.getSubject();

        boolean isSelf = currentUserId.equals(resourceId.toString());

        if (!userContext.isAdmin() && !isSelf) {
            throw new ForbiddenException("You are not allowed to access this resource");
        }
    }

    private Response createGetResponse(UUID id) {

        UserId userId = new UserId(id);
        User user = userManagementUseCase.readById(userId).orElseThrow(NotFoundException::new);
        UserResponseDTO userDto = mapper.toApiModel(user);

        linkService.setSelfLink(userDto);
        Link self = linkService.buildSelfLink(id);
        Link delete = linkService.buildDeleteLink(id);
        Link root = linkService.buildDispatcherLink();

        Response.ResponseBuilder response = Response.ok(userDto);

        if (userContext.isAdmin()) {
            response.header("Link", linkService.buildCollectionLink());
        }

        return response
                .links(self, delete, root)
                .build();
    }
}
