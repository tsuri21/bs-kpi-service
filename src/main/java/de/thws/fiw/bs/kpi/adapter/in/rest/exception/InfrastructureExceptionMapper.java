package de.thws.fiw.bs.kpi.adapter.in.rest.exception;

import de.thws.fiw.bs.kpi.application.domain.exception.InfrastructureException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.logging.Logger;

@Provider
public class InfrastructureExceptionMapper implements ExceptionMapper<InfrastructureException> {

    @Context
    UriInfo uriInfo;

    private static final Logger LOG = Logger.getLogger(InfrastructureExceptionMapper.class);

    @Override
    public Response toResponse(InfrastructureException exception) {
        LOG.error("Infrastructure error occurred:", exception);

        ErrorResponse error = new ErrorResponse(
                "Internal Server Error",
                exception.getMessage(),
                uriInfo.getAbsolutePath().toString()
        );

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .build();
    }
}
