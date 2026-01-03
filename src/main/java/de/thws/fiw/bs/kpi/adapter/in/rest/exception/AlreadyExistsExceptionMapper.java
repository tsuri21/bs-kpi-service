package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

import de.thws.fiw.bs.kpi.application.domain.exception.AlreadyExistsException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AlreadyExistsExceptionMapper implements ExceptionMapper<AlreadyExistsException> {

    @Context UriInfo uriInfo;

    @Override
    public Response toResponse(AlreadyExistsException exception) {
        ErrorResponse error = new ErrorResponse(
                "Conflict",
                exception.getMessage(),
                uriInfo.getAbsolutePath().toString()
        );
        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .build();
    }
}
