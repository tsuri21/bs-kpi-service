package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

import de.thws.fiw.bs.kpi.application.domain.exception.ResourceNotFoundException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ResourceNotFoundExceptionMapper implements ExceptionMapper<ResourceNotFoundException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(ResourceNotFoundException exception) {
        ErrorResponse error = new ErrorResponse(
                "Resource Not Found",
                exception.getMessage(),
                uriInfo.getAbsolutePath().toString()
        );
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error)
                .build();
    }
}
