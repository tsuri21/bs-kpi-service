package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import de.thws.fiw.bs.kpi.application.port.Page;
import de.thws.fiw.bs.kpi.application.port.PageRequest;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import org.jboss.resteasy.specimpl.ResteasyUriBuilderImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HypermediaLinkServiceTest {

    private HypermediaLinkService service;
    private UriInfo uriInfo;
    private HttpHeaders headers;
    private ResourceInfo resourceInfo;
    private final String BASE_URI = "https://api.example.com/api/";

    private static class PlainEntity {
    }

    @BeforeEach
    void setUp() {
        uriInfo = mock(UriInfo.class);
        headers = mock(HttpHeaders.class);
        resourceInfo = mock(ResourceInfo.class);
        service = new HypermediaLinkService(uriInfo, headers, resourceInfo);

        when(uriInfo.getBaseUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(BASE_URI));
        when(headers.getAcceptableMediaTypes()).thenReturn(List.of(MediaType.APPLICATION_JSON_TYPE));
    }

    @Test
    void getMediaType_specificTypesPresent_returnsFirstSpecificType() {
        when(headers.getAcceptableMediaTypes()).thenReturn(List.of(
                MediaType.valueOf("*/*"),
                MediaType.APPLICATION_XML_TYPE,
                MediaType.APPLICATION_JSON_TYPE
        ));

        String result = service.getMediaType();

        assertEquals(MediaType.APPLICATION_XML, result);
    }

    @Test
    void getMediaType_onlyWildcardPresent_returnsDefaultApplicationJson() {
        when(headers.getAcceptableMediaTypes()).thenReturn(List.of(
                MediaType.valueOf("*/*")
        ));

        String result = service.getMediaType();

        assertEquals(MediaType.APPLICATION_JSON, result);
    }

    @Test
    void getRelEntityName_classWithResourceSuffix_returnsStrippedName() {
        String result = service.getRelEntityName(TestResource.class);

        assertEquals("Test", result);
    }

    @Test
    void getRelEntityName_classWithoutResourceSuffix_returnsPlainClassName() {
        String result = service.getRelEntityName(PlainEntity.class);

        assertEquals("PlainEntity", result);
    }

    @Test
    void buildCustomLink_withClassOnly_usesPathAnnotationValue() {
        Link link = service.buildCustomLink(TestResource.class, "getAll", "GET");

        assertEquals(BASE_URI + "tests", link.getUri().toString());
        assertEquals("getAll", link.getRel());
        assertEquals("GET", link.getParams().get("method"));
    }

    @Test
    void buildCustomLink_withClassAndId_appendsIdToAnnotatedPath() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Link link = service.buildCustomLink(TestResource.class, "self", "PUT", id);

        assertEquals(BASE_URI + "tests/11111111-2222-3333-4444-555555555555", link.getUri().toString());
        assertEquals("self", link.getRel());
        assertEquals("PUT", link.getParams().get("method"));
    }

    @Test
    void buildLocationUri_withClassAndId_returnsFullUri() {
        UUID id = UUID.randomUUID();

        URI result = service.buildLocationUri(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.toString());
    }

    @Test
    void buildLocationUri_withContextAndId_returnsFullUri() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        URI result = service.buildLocationUri(id);

        assertEquals(BASE_URI + "tests/" + id, result.toString());
    }

    @Test
    void buildSelfLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.buildSelfLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void buildSelfLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildSelfLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void buildCollectionLink_withClass_returnsCorrectLink() {
        String result = service.buildCollectionLink(TestResource.class);

        String expected = "<" + BASE_URI + "tests>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"";
        assertEquals(expected, result);
    }

    @Test
    void buildCollectionLink_withClassAndParams_returnsUriTemplateLinkAsString() {
        String[] queryParams = {"name", "category"};

        String result = service.buildCollectionLink(TestResource.class, queryParams);

        String expected = "<" + BASE_URI + "tests{?name,category}>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"; templated=\"true\"";
        assertEquals(expected, result);
    }

    @Test
    void buildCollectionLink_withContext_returnsCorrectLink() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        String result = service.buildCollectionLink();

        String expected = "<" + BASE_URI + "tests>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"";
        assertEquals(expected, result);
    }

    @Test
    void buildCollectionLink_withContextAndParams_returnsUriTemplateLinkAsString() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        String result = service.buildCollectionLink("q");

        String expected = "<" + BASE_URI + "tests{?q}>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"; templated=\"true\"";
        assertEquals(expected, result);
    }

    @Test
    void buildCreateLink_withClass_returnsCorrectLink() {
        Link result = service.buildCreateLink(TestResource.class);

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("createTest", result.getRel());
        assertEquals("POST", result.getParams().get("method"));
    }

    @Test
    void buildCreateLink_withContext_returnsCorrectLink() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildCreateLink();

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("createTest", result.getRel());
        assertEquals("POST", result.getParams().get("method"));
    }

    @Test
    void buildUpdateLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.buildUpdateLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PUT", result.getParams().get("method"));
    }

    @Test
    void buildUpdateLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildUpdateLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PUT", result.getParams().get("method"));
    }

    @Test
    void buildPartialUpdateLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.buildPartialUpdateLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PATCH", result.getParams().get("method"));
    }

    @Test
    void buildPartialUpdateLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildPartialUpdateLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PATCH", result.getParams().get("method"));
    }

    @Test
    void buildDeleteLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.buildDeleteLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("deleteTest", result.getRel());
        assertEquals("DELETE", result.getParams().get("method"));
    }

    @Test
    void buildDeleteLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildDeleteLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("deleteTest", result.getRel());
        assertEquals("DELETE", result.getParams().get("method"));
    }

    @Test
    void buildDescriptionLink_withClass_returnsLinkToSchema() {
        Link result = service.buildDescriptionLink(TestResource.class);

        assertEquals(BASE_URI + "tests/schema", result.getUri().toString());
        assertEquals("describedby", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
        assertEquals("application/schema+json", result.getType());
    }

    @Test
    void buildDescriptionLink_withContext_returnsLinkToSchema() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.buildDescriptionLink();

        assertEquals(BASE_URI + "tests/schema", result.getUri().toString());
        assertEquals("describedby", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
        assertEquals("application/schema+json", result.getType());
    }

    @Test
    void buildPaginationLinks_firstPage_returnNextLink() {
        String requestUri = BASE_URI + "tests?name=Test&sort=date";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(1, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.buildPaginationLinks(page);

        assertEquals(1, links.length);

        Link next = findLinkByRel(links, "next");

        assertEquals(BASE_URI + "tests?name=Test&sort=date&page=2", next.getUri().toString());
        assertEquals("next", next.getRel());
        assertEquals("GET", next.getParams().get("method"));
    }

    @Test
    void buildPaginationLinks_middlePage_returnsPrevAndNextLinks() {
        String requestUri = BASE_URI + "tests?q=Test&page=5";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(5, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.buildPaginationLinks(page);

        assertEquals(2, links.length);

        Link prev = findLinkByRel(links, "prev");
        Link next = findLinkByRel(links, "next");

        assertEquals(BASE_URI + "tests?q=Test&page=4", prev.getUri().toString());
        assertEquals("prev", prev.getRel());
        assertEquals("GET", prev.getParams().get("method"));

        assertEquals(BASE_URI + "tests?q=Test&page=6", next.getUri().toString());
        assertEquals("next", next.getRel());
        assertEquals("GET", next.getParams().get("method"));
    }

    @Test
    void buildPaginationLinks_lastPage_returnsPrevLink() {
        String requestUri = BASE_URI + "tests?page=10";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(10, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.buildPaginationLinks(page);

        assertEquals(1, links.length);

        Link prev = findLinkByRel(links, "prev");

        assertEquals(BASE_URI + "tests?page=9", prev.getUri().toString());
        assertEquals("prev", prev.getRel());
        assertEquals("GET", prev.getParams().get("method"));
    }

    @Test
    void setSelfLink_validDto_setsLinkInDto() {
        UUID id = UUID.randomUUID();
        TestDTO dto = new TestDTO();
        dto.setId(id);

        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        service.setSelfLink(dto);

        assertNotNull(dto.getSelfLink());
        assertEquals(BASE_URI + "tests/" + id, dto.getSelfLink().getHref());
        assertEquals("self", dto.getSelfLink().getRel());
        assertEquals("GET", dto.getSelfLink().getMethod());
        assertEquals("application/json", dto.getSelfLink().getType());
    }

    @Test
    void setSelfLinks_listOfDtos_setsLinksForAll() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        TestDTO dto1 = new TestDTO();
        dto1.setId(UUID.randomUUID());
        TestDTO dto2 = new TestDTO();
        dto2.setId(UUID.randomUUID());

        List<TestDTO> dtos = List.of(dto1, dto2);

        service.setSelfLinks(dtos);
        assertNotNull(dto1.getSelfLink());
        assertNotNull(dto2.getSelfLink());
        assertNotEquals(dto1.getSelfLink().getHref(), dto2.getSelfLink().getHref());
    }

    private Link findLinkByRel(Link[] links, String rel) {
        return Arrays.stream(links)
                .filter(l -> l.getRel().equals(rel))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Link not found"));
    }
}