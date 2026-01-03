package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class IllegalArgumentExceptionMapper implements ExceptionMapper<IllegalArgumentException> {

    @Context
    UriInfo uriInfo;

    @Override
    public Response toResponse(IllegalArgumentException exception) {

        ErrorResponse error = new ErrorResponse(
                "Bad Request",
                exception.getMessage(),
                uriInfo.getAbsolutePath().toString()
        );

        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error)
                .build();
    }
}
