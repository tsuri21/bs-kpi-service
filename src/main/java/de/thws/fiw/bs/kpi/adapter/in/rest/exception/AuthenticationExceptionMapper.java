package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

import de.thws.fiw.bs.kpi.application.domain.exception.AuthenticationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationExceptionMapper implements ExceptionMapper<AuthenticationException> {

    @Context UriInfo uriInfo;

    @Override
    public Response toResponse(AuthenticationException exception) {
        ErrorResponse error = new ErrorResponse(
                "Unauthorized",
                exception.getMessage(),
                uriInfo.getAbsolutePath().toString()
        );
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(error)
                .build();
    }
}
