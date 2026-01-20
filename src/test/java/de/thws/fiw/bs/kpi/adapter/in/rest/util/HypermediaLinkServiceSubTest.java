package de.thws.fiw.bs.kpi.adapter.in.rest.util;

import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.UriInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class HypermediaLinkServiceSubTest {

    private HypermediaLinkService linkService;
    private UUID testId;
    private URI testSelfUri;
    private URI testSelfUriID;
    private final String BASE_URI = "https://api.example.com/api/layer1/1";

    @BeforeEach
    public void setUp() {
        UriInfo uriInfo = mock(UriInfo.class);
        HttpHeaders headers = mock(HttpHeaders.class);
        ResourceInfo resourceInfo = mock(ResourceInfo.class);
        linkService = new HypermediaLinkService(uriInfo, headers, resourceInfo);
        testId = UUID.fromString("771dee4e-c7bb-4f1a-b899-1c2c20424b9e");
        testSelfUri = URI.create(BASE_URI + "/layer2");
        testSelfUriID = URI.create(testSelfUri + "/" + testId);
    }

    @Test
    void buildSelfLinkSubLayerBack_withSelfUriAndResourceClass_returnsCorrectLink() {
        String result = linkService.buildSelfLinkSubLayerBack(testSelfUri, TestResource.class);

        String expected = "<" + BASE_URI + ">; method=\"GET\"; rel=\"getTest\"; type=\"application/json\"";

        assertEquals(expected, result);
    }

    @Test
    void buildSelfLinkSub_withSelfUri_returnsCorrectLink() {
        Link result = linkService.buildSelfLinkSub(testSelfUri);

        assertEquals(testSelfUri, result.getUri());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void buildSelfLinkSub_withSelfUriAndId_returnsCorrectLink() {
        Link result = linkService.buildSelfLinkSub(testSelfUri, testId);

        assertEquals(testSelfUriID, result.getUri());
        assertEquals("self", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void buildEvaluationLinkSub_returnsCorrectLink() {
        Link result = linkService.buildEvaluationLinkSub(testSelfUri, TestResource.class);

        URI expected = URI.create(testSelfUri + "/evaluation");
        assertEquals(expected, result.getUri());
        assertEquals("evaluateTest", result.getRel());
        assertEquals("GET", result.getParams().get("method"));
    }

    @Test
    void buildCreateLinkSub_returnsCorrectLink() {
        Link result = linkService.buildCreateLinkSub(testSelfUri, TestResource.class);

        assertEquals(testSelfUri, result.getUri());
        assertEquals("createTest", result.getRel());
        assertEquals("POST", result.getParams().get("method"));
    }

    @Test
    void buildUpdateLinkSub_returnsCorrectLink() {
        Link result = linkService.buildUpdateLinkSub(testSelfUriID, TestResource.class);

        assertEquals(testSelfUriID, result.getUri());
        assertEquals("updateTest", result.getRel());
        assertEquals("PUT", result.getParams().get("method"));
    }

    @Test
    void buildDeleteLinkSub_returnsCorrectLink() {
        Link result = linkService.buildDeleteLinkSub(testSelfUriID, TestResource.class);

        assertEquals(testSelfUriID, result.getUri());
        assertEquals("deleteTest", result.getRel());
        assertEquals("DELETE", result.getParams().get("method"));
    }

    @Test
    void buildLocationUriSub_returnsCorrectUri() {
        URI result = linkService.buildLocationUriSub(testSelfUri, testId);

        assertEquals(testSelfUriID, result);
    }

    @Test
    void buildCollectionLinkSub_withoutId_returnsTemplatedLinkString() {
        String result = linkService.buildCollectionLinkSub(testSelfUriID, TestResource.class, "test");

        String expected = "<" + testSelfUri + "{?test}>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"; templated=\"true\"";

        assertEquals(expected, result);
    }

    @Test
    void buildCollectionLinkSub_withId_returnsCorrectLinkString() {
        String result = linkService.buildCollectionLinkSub(testSelfUri, testId, TestResource.class, "test");

        String expected = "<" + testSelfUri + "{?test}>; method=\"GET\"; rel=\"getAllTestItems\"; type=\"application/json\"; templated=\"true\"";

        assertEquals(expected, result);
    }

    @Test
    void setSelfLinkSub_updatesDtoWithCorrectLink() {
        TestDTO dto = new TestDTO();
        dto.setId(testId);

        linkService.setSelfLinkSub(dto, testSelfUriID);

        assertNotNull(dto.getSelfLink());
        assertEquals(testSelfUriID.toString(), dto.getSelfLink().getHref());
        assertEquals("self", dto.getSelfLink().getRel());
    }

    @Test
    void setSelfLinksSub_updatesAllDtosInList() {
        TestDTO dto1 = new TestDTO();
        dto1.setId(UUID.randomUUID());
        TestDTO dto2 = new TestDTO();
        dto2.setId(UUID.randomUUID());
        List<TestDTO> dtos = List.of(dto1, dto2);

        linkService.setSelfLinksSub(dtos, testSelfUri);

        assertEquals(testSelfUri + "/" + dto1.getId(), dto1.getSelfLink().getHref());
        assertEquals(testSelfUri + "/" + dto2.getId(), dto2.getSelfLink().getHref());
    }
}
