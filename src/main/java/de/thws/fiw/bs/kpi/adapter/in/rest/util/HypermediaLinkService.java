package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import de.thws.fiw.bs.kpi.adapter.in.rest.model.AbstractDTO;
import de.thws.fiw.bs.kpi.adapter.in.rest.model.SelfLink;
import de.thws.fiw.bs.kpi.adapter.in.rest.resource.RootResource;
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

    public Link buildDispatcherLink() {
        return buildCustomLink(RootResource.class, "start", "GET");
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

    public String buildCollectionLink(String... queryParams) {
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

    public Link buildCustomLink(Class<?> targetRes, String rel, String method) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    public Link buildCustomLink(Class<?> targetRes, String rel, String method, UUID id) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes).path(id.toString()))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    public Link buildCustomLink(Class<?> targetRes, String function, String rel, String method) {
        return Link.fromUriBuilder(uriInfo.getBaseUriBuilder().path(targetRes).path(targetRes, function))
                .rel(rel)
                .param("method", method)
                .type(getMediaType())
                .build();
    }

    public void setSelfLink(AbstractDTO dto, Class<?> pathClass) {
        String href = uriInfo.getBaseUriBuilder()
                .path(pathClass)
                .path(dto.getId().toString())
                .build()
                .toString();

        dto.setSelfLink(new SelfLink(href, "self", getMediaType(), "GET"));
    }

    public void setSelfLinks(List<? extends AbstractDTO> dtos, Class<?> pathClass) {
        dtos.forEach(t -> setSelfLink(t, pathClass));
    }


    public String buildSelfLinkSubLayerBack(URI selfUri, Class<?> pathClass) {
        URI layerUp = UriBuilder.fromUri(selfUri)
                .path("..")
                .build()
                .normalize();

        String collectionUriStr = layerUp.toString();
        if (collectionUriStr.endsWith("/")) {
            collectionUriStr = collectionUriStr.substring(0, collectionUriStr.length() - 1);
        }

        String rel = "get" + getRelEntityName(pathClass);
        String type = getMediaType();

        StringBuilder linkBuilder = new StringBuilder("<").append(collectionUriStr);

        linkBuilder.append(">; ");
        linkBuilder.append("method=\"GET\"; ");
        linkBuilder.append("rel=\"").append(rel).append("\"; ");
        linkBuilder.append("type=\"").append(type).append("\"");

        return linkBuilder.toString();
    }

    public Link buildSelfLinkSub(URI selfUri) {
        return Link.fromUri(selfUri)
                .rel("self")
                .param("method", "GET")
                .type(getMediaType())
                .build();
    }

    public Link buildSelfLinkSub(URI collectionUri, UUID id) {
        URI selfUri = UriBuilder.fromUri(collectionUri)
                .path(id.toString())
                .build();

        return Link.fromUri(selfUri)
                .rel("self")
                .param("method", "GET")
                .type(getMediaType())
                .build();
    }

    public Link buildEvaluationLinkSub(URI selfUri, Class<?> pathClass) {
        URI evaluationUri = UriBuilder.fromUri(selfUri)
                .path("evaluation")
                .build();

        return Link.fromUri(evaluationUri)
                .rel("evaluate" + getRelEntityName(pathClass))
                .param("method", "GET")
                .type(getMediaType())
                .build();
    }

    public Link buildCreateLinkSub(URI selfUri, Class<?> pathClass) {
        return Link.fromUri(selfUri)
                .rel("create" + getRelEntityName(pathClass))
                .param("method", "POST")
                .type(getMediaType())
                .build();
    }

    public Link buildUpdateLinkSub(URI selfUri, Class<?> pathClass) {
        return Link.fromUri(selfUri)
                .rel("update" + getRelEntityName(pathClass))
                .param("method", "PUT")
                .type(getMediaType())
                .build();
    }

    public Link buildDeleteLinkSub(URI selfUri, Class<?> pathClass) {
        return Link.fromUri(selfUri)
                .rel("delete" + getRelEntityName(pathClass))
                .param("method", "DELETE")
                .type(getMediaType())
                .build();
    }

    public URI buildLocationUriSub(URI selfUri, UUID id) {
        return UriBuilder.fromUri(selfUri)
                .path(id.toString())
                .build();
    }

    public String buildCollectionLinkSub(URI selfUri, Class<?> pathClass, String... queryParams) {
        URI collectionUri = UriBuilder.fromUri(selfUri)
                .path("..")
                .build()
                .normalize();

        String collectionUriStr = collectionUri.toString();
        if (collectionUriStr.endsWith("/")) {
            collectionUriStr = collectionUriStr.substring(0, collectionUriStr.length() - 1);
        }

        String rel = "getAll" + getRelEntityName(pathClass) + "Items";
        String type = getMediaType();

        StringBuilder linkBuilder = new StringBuilder("<").append(collectionUriStr);

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

    public String buildCollectionLinkSub(URI collectionUri, UUID id, Class<?> pathClass, String... queryParams) {
        URI selfUri = UriBuilder.fromUri(collectionUri).path(id.toString()).build();
        return buildCollectionLinkSub(selfUri, pathClass, queryParams);
    }

    public void setSelfLinkSub(AbstractDTO dto, URI baseUri) {
        UriBuilder builder = UriBuilder.fromUri(baseUri);

        if (!baseUri.toString().contains(dto.getId().toString())) {
            builder.path(dto.getId().toString());
        }

        String href = builder.build().toString();

        dto.setSelfLink(new SelfLink(href, "self", getMediaType(), "GET"));
    }

    public void setSelfLinksSub(List<? extends AbstractDTO> dtos, URI collectionUri) {
        if (dtos != null && !dtos.isEmpty()) {
            dtos.forEach(dto -> setSelfLinkSub(dto, collectionUri));
        }
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
