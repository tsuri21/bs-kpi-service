package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.authentication.TokenResponseDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.util.HypermediaLinkService;
import de.thws.fiw.bs.kpi.application.domain.model.user.Username;
import de.thws.fiw.bs.kpi.application.port.in.AuthenticationUseCase;
import jakarta.annotation.security.PermitAll;
import jakarta.inject.Inject;
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

        Link currentUser = linkService.buildCustomLink(UserResource.class, "getCurrent", "getCurrentUser", "GET");
        Link root = linkService.buildDispatcherLink();

        return Response.ok(new TokenResponseDTO(token)).links(root, currentUser).build();
    }
}
