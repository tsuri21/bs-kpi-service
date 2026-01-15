package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.SelfLink;
import de.thws.fiw.bs.kpi.application.port.Page;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequestScoped
public class HypermediaLinkService {

    private final UriInfo uriInfo;
    private final HttpHeaders headers;
    private final ResourceInfo resourceInfo;

    @Inject
    public HypermediaLinkService(@Context UriInfo uriInfo, @Context HttpHeaders headers, @Context ResourceInfo resourceInfo) {
        this.uriInfo = uriInfo;
        this.headers = headers;
        this.resourceInfo = resourceInfo;
    }

    public URI buildLocationUri(Class<?> pathClass, UUID id) {
        return uriInfo.getBaseUriBuilder().path(pathClass).path(id.toString()).build();
    }

    public URI buildLocationUri(UUID id) {
        return buildLocationUri(resourceInfo.getResourceClass(), id);
    }

    public Link buildSelfLink(Class<?> pathClass, UUID id) {
        return buildCustomLink(pathClass, "self", "GET", id);
    }

    public Link buildSelfLink(UUID id) {
        return buildSelfLink(resourceInfo.getResourceClass(), id);
    }

    public String buildCollectionLink(Class<?> pathClass, String... queryParams) {
        String base = uriInfo.getBaseUriBuilder()
                .path(pathClass)
                .build()
                .toString();

        String rel = "getAll" + getRelEntityName(pathClass) + "Items";
        String type = getMediaType();

        StringBuilder linkBuilder = new StringBuilder();
        linkBuilder.append("<").append(base);

        boolean isTemplated = false;

        if (queryParams != null && queryParams.length > 0) {
            linkBuilder.append("{?").append(String.join(",", queryParams)).append("}");
            isTemplated = true;
        }

        linkBuilder.append(">; ");
        linkBuilder.append("method=\"GET\"; ");
        linkBuilder.append("rel=\"").append(rel).append("\"; ");
        linkBuilder.append("type=\"").append(type).append("\"");

        if (isTemplated) {
            linkBuilder.append("; templated=\"true\"");
        }

        return linkBuilder.toString();
    }

    public String buildCollectionLink(String ...queryParams) {
        return buildCollectionLink(resourceInfo.getResourceClass(), queryParams);
    }

    public Link buildCreateLink(Class<?> pathClass) {
        return buildCustomLink(pathClass, "create" + getRelEntityName(pathClass), "POST");
    }

    public Link buildCreateLink() {
        return buildCreateLink(resourceInfo.getResourceClass());
    }

    public Link buildUpdateLink(Class<?> pathClass, UUID id) {
        return buildCustomLink(pathClass, "update" + getRelEntityName(pathClass), "PUT", id);
    }

    public Link buildUpdateLink(UUID id) {
        return buildUpdateLink(resourceInfo.getResourceClass(), id);
    }

    public Link buildPartialUpdateLink(Class<?> pathClass, UUID id) {
        return buildCustomLink(pathClass, "update" + getRelEntityName(pathClass), "PATCH", id);
    }

    public Link buildPartialUpdateLink(UUID id) {
        return buildPartialUpdateLink(resourceInfo.getResourceClass(), id);
    }

    public Link buildDeleteLink(Class<?> pathClass, UUID id) {
        return buildCustomLink(pathClass, "delete" + getRelEntityName(pathClass), "DELETE", id);
    }

    public Link buildDeleteLink(UUID id) {
        return buildDeleteLink(resourceInfo.getResourceClass(), id);
    }

    public Link buildDescriptionLink(Class<?> pathClass) {
        URI schemaUri = uriInfo.getBaseUriBuilder().path(pathClass).path("schema").build();
        return Link.fromUri(schemaUri)
                .rel("describedby")
                .param("method", "GET")
                .type("application/schema+json")
                .build();
    }

    public Link buildDescriptionLink() {
        return buildDescriptionLink(resourceInfo.getResourceClass());
    }

    public Link[] buildPaginationLinks(Page<?> page) {
        List<Link> links = new ArrayList<>();
        String mediaType = getMediaType();
        int currentPage = page.pageRequest().pageNumber();

        if (page.hasPrevious()) {
            links.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                            .replaceQueryParam("page", currentPage - 1))
                    .rel("prev")
                    .param("method", "GET")
                    .type(mediaType)
                    .build());
        }

        if (page.hasNext()) {
            links.add(Link.fromUriBuilder(uriInfo.getRequestUriBuilder()
                            .replaceQueryParam("page", currentPage + 1))
                    .rel("next")
                    .param("method", "GET")
                    .type(mediaType)
                    .build());
        }

        return links.toArray(new Link[0]);
    }

    public void setSelfLink(AbstractDTO dto) {
        String href = uriInfo.getBaseUriBuilder()
                .path(resourceInfo.getResourceClass())
                .path(dto.getId().toString())
                .build()
                .toString();

        dto.setSelfLink(new SelfLink(href, "self", getMediaType(), "GET"));
    }

    public void setSelfLinks(List<? extends AbstractDTO> dtos) {
        dtos.forEach(this::setSelfLink);
    }

    Link buildCustomLink(Class<?> targetRes, String rel, String method) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    Link buildCustomLink(Class<?> targetRes, String rel, String method, UUID id) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes).path(id.toString()))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    String getMediaType() {
        return headers.getAcceptableMediaTypes().stream()
                .map(MediaType::toString)
                .filter(t -> !t.contains("*/*"))
                .findFirst()
                .orElse(MediaType.APPLICATION_JSON);
    }

    String getRelEntityName(Class<?> pathClass) {
        return pathClass.getSimpleName().replace("Resource", "");
    }
}
