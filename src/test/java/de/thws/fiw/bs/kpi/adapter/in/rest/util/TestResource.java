package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@Path("/tests")
public class TestResource {

    @GET
    public String dummy() {
        return "dummy";
    }
}
