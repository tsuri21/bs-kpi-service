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

    public URI createLocationUri(Class<?> pathClass, UUID id) {
        return uriInfo.getBaseUriBuilder().path(pathClass).path(id.toString()).build();
    }

    public URI createLocationUri(UUID id) {
        return createLocationUri(resourceInfo.getResourceClass(), id);
    }

    public Link createSelfLink(Class<?> pathClass, UUID id) {
        return createCustomLink(pathClass, "self", "GET", id);
    }

    public Link createSelfLink(UUID id) {
        return createSelfLink(resourceInfo.getResourceClass(), id);
    }

    public Link createGetAllLink(Class<?> pathClass) {
        return createCustomLink(pathClass, "get" + getRelEntityName(pathClass) + "Items", "GET");
    }

    public Link createGetAllLink() {
        return createGetAllLink(resourceInfo.getResourceClass());
    }

    public Link createCreateLink(Class<?> pathClass) {
        return createCustomLink(pathClass, "create" + getRelEntityName(pathClass), "POST");
    }

    public Link createCreateLink() {
        return createCreateLink(resourceInfo.getResourceClass());
    }

    public Link createUpdateLink(Class<?> pathClass, UUID id) {
        return createCustomLink(pathClass, "update" + getRelEntityName(pathClass), "PUT", id);
    }

    public Link createUpdateLink(UUID id) {
        return createUpdateLink(resourceInfo.getResourceClass(), id);
    }

    public Link createDeleteLink(Class<?> pathClass, UUID id) {
        return createCustomLink(pathClass, "delete" + getRelEntityName(pathClass), "DELETE", id);
    }

    public Link createDeleteLink(UUID id) {
        return createDeleteLink(resourceInfo.getResourceClass(), id);
    }

    public String createSearchTemplateLink(Class<?> pathClass, String... params) {
        String base = uriInfo.getBaseUriBuilder().path(pathClass).build().toString();
        String queryParams = String.join(",", params);
        String rel = "search" + getRelEntityName(pathClass) + "Items";
        String type = getMediaType();
        return String.format("<%s{?%s}>; method=\"GET\"; rel=\"%s\"; type=\"%s\"", base, queryParams, rel, type);
    }

    public String createSearchTemplateLink(String... params) {
        return createSearchTemplateLink(resourceInfo.getResourceClass(), params);
    }

    public Link createDescriptionLink(Class<?> pathClass) {
        URI schemaUri = uriInfo.getBaseUriBuilder().path(pathClass).path("schema").build();
        return Link.fromUri(schemaUri)
                .rel("describedby")
                .param("method", "GET")
                .type("application/schema+json")
                .build();
    }

    public Link createDescriptionLink() {
        return createDescriptionLink(resourceInfo.getResourceClass());
    }

    public Link[] createPaginationLinks(Page<?> page) {
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

    Link createCustomLink(Class<?> targetRes, String rel, String method) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    Link createCustomLink(Class<?> targetRes, String rel, String method, UUID id) {
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
