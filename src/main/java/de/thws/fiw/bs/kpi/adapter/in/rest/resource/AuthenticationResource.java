package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.mapper.RoleApiMapper;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.user.CreateUserDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.authentication.TokenResponseDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.UserContext;
import de.thws.fiw.bs.kpi.application.domain.model.user.UserId;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.in.AuthenticationUseCase;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Path("auth")
@PermitAll
public class AuthenticationResource {

    @Inject
    AuthenticationUseCase authenticationUseCase;

    @Inject
    HypermediaLinkService linkService;

    @Inject
    UserContext userContext;

    @Inject
    RoleApiMapper mapper;

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.toLowerCase().startsWith("basic ")) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity("Missing or invalid Authorization header").build();
        }

        String[] headerParts = authHeader.split("\\s+");
        if (headerParts.length != 2) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        String base64Credentials = headerParts[1];

        String decoded = new String(
                Base64.getDecoder().decode(base64Credentials),
                StandardCharsets.UTF_8
        );

        String[] parts = decoded.split(":", 2);
        if (parts.length != 2) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }

        String username = parts[0];
        String password = parts[1];

        String token = authenticationUseCase.login(new Username(username), password);

        // TODO: link on /users/me
        Link root = linkService.buildDispatcherLink();
        Response.ResponseBuilder response = Response.ok(new TokenResponseDTO(token)).links(root);

        if (userContext.isAdmin()) {
            response.header("Link", linkService.buildCollectionLink());
        }

        return response.build();
    }

    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response register(@Valid CreateUserDTO createUserDto) {
        UserId id = authenticationUseCase.register(
                new Username(createUserDto.getUsername()),
                createUserDto.getPassword(),
                mapper.toDomainModel(createUserDto.getRole())
        );

        // TODO: login link
        Link self = linkService.buildSelfLink(UserResource.class, id.value());
        return Response.created(linkService.buildLocationUri(UserResource.class, id.value()))
                .links(self)
                .build();
    }
}
