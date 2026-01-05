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
    void createCustomLink_withClassOnly_usesPathAnnotationValue() {
        Link link = service.createCustomLink(TestResource.class, "getAll", "GET");

        assertEquals(BASE_URI + "tests", link.getUri().toString());
        assertEquals("getAll", link.getRel());
        assertEquals("GET", link.getParams().get("method"));
    }

    @Test
    void createCustomLink_withClassAndId_appendsIdToAnnotatedPath() {
        UUID id = UUID.fromString("11111111-2222-3333-4444-555555555555");
        Link link = service.createCustomLink(TestResource.class, "self", "PUT", id);

        assertEquals(BASE_URI + "tests/11111111-2222-3333-4444-555555555555", link.getUri().toString());
        assertEquals("self", link.getRel());
        assertEquals("PUT", link.getParams().get("method"));
    }

    @Test
    void createLocationUri_withClassAndId_returnsFullUri() {
        UUID id = UUID.randomUUID();

        URI result = service.createLocationUri(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.toString());
    }

    @Test
    void createLocationUri_withContextAndId_returnsFullUri() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        URI result = service.createLocationUri(id);

        assertEquals(BASE_URI + "tests/" + id, result.toString());
    }

    @Test
    void createSelfLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.createSelfLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void createSelfLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.createSelfLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void createGetAllLink_withClass_returnsCorrectLink() {
        Link result = service.createGetAllLink(TestResource.class);

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("getAllTest", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void createGetAllLink_withContext_returnsCorrectLink() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();
        Link result = service.createGetAllLink();

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("getAllTest", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void createCreateLink_withClass_returnsCorrectLink() {
        Link result = service.createCreateLink(TestResource.class);

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("createTest", result.getRel());
        assertEquals("POST", result.getParams().get("method"));
    }

    @Test
    void createCreateLink_withContext_returnsCorrectLink() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.createCreateLink();

        assertEquals(BASE_URI + "tests", result.getUri().toString());
        assertEquals("createTest", result.getRel());
        assertEquals("POST", result.getParams().get("method"));
    }

    @Test
    void createUpdateLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.createUpdateLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PUT", result.getParams().get("method"));
    }

    @Test
    void createUpdateLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.createUpdateLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("updateTest", result.getRel());
        assertEquals("PUT", result.getParams().get("method"));
    }

    @Test
    void createDeleteLink_withClassAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();

        Link result = service.createDeleteLink(TestResource.class, id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("deleteTest", result.getRel());
        assertEquals("DELETE", result.getParams().get("method"));
    }

    @Test
    void createDeleteLink_withContextAndId_returnsCorrectLink() {
        UUID id = UUID.randomUUID();
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.createDeleteLink(id);

        assertEquals(BASE_URI + "tests/" + id, result.getUri().toString());
        assertEquals("deleteTest", result.getRel());
        assertEquals("DELETE", result.getParams().get("method"));
    }

    @Test
    void createSearchTemplateLink_withClassAndParams_returnsUriTemplateLinkAsString() {
        String[] queryParams = {"name", "category"};

        String result = service.createSearchTemplateLink(TestResource.class, queryParams);

        String expected = "<" + BASE_URI + "tests{?name,category}>; method=\"GET\"; rel=\"searchTest\"; type=\"application/json\"";
        assertEquals(expected, result);
    }

    @Test
    void createSearchTemplateLink_withContextAndParams_returnsUriTemplateLinkAsString() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        String result = service.createSearchTemplateLink("q");

        String expected = "<" + BASE_URI + "tests{?q}>; method=\"GET\"; rel=\"searchTest\"; type=\"application/json\"";
        assertEquals(expected, result);
    }

    @Test
    void createDescriptionLink_withClass_returnsLinkToSchema() {
        Link result = service.createDescriptionLink(TestResource.class);

        assertEquals(BASE_URI + "tests/schema", result.getUri().toString());
        assertEquals("describedby", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
        assertEquals("application/schema+json", result.getType());
    }

    @Test
    void createDescriptionLink_withContext_returnsLinkToSchema() {
        doReturn(TestResource.class).when(resourceInfo).getResourceClass();

        Link result = service.createDescriptionLink();

        assertEquals(BASE_URI + "tests/schema", result.getUri().toString());
        assertEquals("describedby", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
        assertEquals("application/schema+json", result.getType());
    }

    @Test
    void createPaginationLinks_firstPage_returnNextLink() {
        String requestUri = BASE_URI + "tests?name=Test&sort=date";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(1, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.createPaginationLinks(page);

        assertEquals(1, links.length);

        Link next = findLinkByRel(links, "next");

        assertEquals(BASE_URI + "tests?name=Test&sort=date&page=2", next.getUri().toString());
        assertEquals("next", next.getRel());
        assertEquals("GET", next.getParams().get("method"));
    }

    @Test
    void createPaginationLinks_middlePage_returnsPrevAndNextLinks() {
        String requestUri = BASE_URI + "tests?q=Test&page=5";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(5, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.createPaginationLinks(page);

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
    void createPaginationLinks_lastPage_returnsPrevLink() {
        String requestUri = BASE_URI + "tests?page=10";
        when(uriInfo.getRequestUriBuilder()).thenAnswer(inv -> new ResteasyUriBuilderImpl().uri(requestUri));

        PageRequest pageRequest = new PageRequest(10, 10);
        Page<String> page = new Page<>(List.of("item"), pageRequest, 100);

        Link[] links = service.createPaginationLinks(page);

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