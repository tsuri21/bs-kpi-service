package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Request;
import jakarta.ws.rs.core.Response;

@RequestScoped
public class CachingUtil {

    public static Response.ResponseBuilder getConditionalBuilder(Request request, Object dto) {
        EntityTag etag = new EntityTag(Integer.toHexString(dto.hashCode()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(etag);

        if (builder != null) {
            return builder.tag(etag);
        }

        return Response.ok(dto).tag(etag);
    }

    public void checkPreconditions(Request request, Object currentDomainOrDto) {
        EntityTag currentEtag = new EntityTag(Integer.toHexString(currentDomainOrDto.hashCode()));

        Response.ResponseBuilder builder = request.evaluatePreconditions(currentEtag);

        if (builder != null) {
            throw new WebApplicationException(builder.build());
        }
    }
}

