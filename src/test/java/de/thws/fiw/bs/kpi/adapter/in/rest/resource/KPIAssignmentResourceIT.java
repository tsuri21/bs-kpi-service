package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KPIAssignmentResourceIT {


    private static String ADMIN_TOKEN;
    private static String ADMIN_ID;
    private static String USER_TOKEN;

    private static String PROJECT_ID;
    private static String KPI_ALPHA_ID;
    private static String ASSIGNMENT_ID;

    private static final String BASE_URL = "http://localhost:8080/api";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;

        ADMIN_ID = registerUser("admin-boss", "adminPass", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-boss", "adminPass");
        registerUser("normal-worker", "workerPass", "Member");
        USER_TOKEN = fetchToken("normal-worker", "workerPass");

        Response pRes = given().auth().oauth2(ADMIN_TOKEN).contentType(ContentType.JSON)
                .body("{\"name\": \"Project with Repo\", \"repoUrl\": \"https://github.com/user/repo\"}").post("/projects");
        PROJECT_ID = pRes.getHeader("Location").substring(pRes.getHeader("Location").lastIndexOf("/") + 1);

        for (int i = 1; i <= 10; i++) {
            String kpiId = createBaseKPI("Alpha Assignment KPI " + i);
            createAssignment(kpiId);
            if (i == 1) KPI_ALPHA_ID = kpiId;
        }

        for (int i = 1; i <= 5; i++) {
            String kpiId = createBaseKPI("Beta Assignment KPI " + i);
            createAssignment(kpiId);
        }
    }

    @AfterAll
    static void tearDown() {
        while (true) {
            Response res = given().auth().oauth2(ADMIN_TOKEN).get("/projects");
            List<String> ids = res.jsonPath().getList("id");

            if (ids == null || ids.isEmpty()) break;

            for (String id : ids) {
                given().auth().oauth2(ADMIN_TOKEN).delete("/projects/" + id);
            }
        }

        while (true) {
            Response res = given().auth().oauth2(ADMIN_TOKEN).get("/kpis");
            List<String> ids = res.jsonPath().getList("id");

            if (ids == null || ids.isEmpty()) break;

            for (String id : ids) {
                given().auth().oauth2(ADMIN_TOKEN).delete("/kpis/" + id);
            }

            while (true) {
                Response uRes = given()
                        .auth().oauth2(ADMIN_TOKEN)
                        .get("/users");

                List<String> uIds = uRes.jsonPath().getList("findAll { it.username != '" + "admin-boss" + "' }.id");

                if (uIds == null || uIds.isEmpty()) {
                    break;
                }

                for (String uId : uIds) {
                    given()
                            .auth().oauth2(ADMIN_TOKEN)
                            .delete("/users/" + uId)
                            .then()
                            .statusCode(anyOf(is(204), is(404)));
                }
            }
            if (ADMIN_ID != null) {
                given()
                        .auth().oauth2(ADMIN_TOKEN)
                        .delete("/users/" + ADMIN_ID)
                        .then()
                        .statusCode(anyOf(is(204), is(404)));
            }
        }


        while (true) {
            Response kRes = given().auth().oauth2(ADMIN_TOKEN).get("/kpis");
            List<String> kIds = kRes.jsonPath().getList("id");

            if (kIds == null || kIds.isEmpty()) break;

            for (String kId : kIds) {
                given().auth().oauth2(ADMIN_TOKEN).delete("/kpis/" + kId);
            }
        }
    }

    @Test
    @Order(1)
    void getAll_pageOne_hasNext() {
        List<String> links = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"getProject\"")));
        assertThat(links, hasItem(containsString("rel=\"next\"")));
        assertThat(links, hasItem(containsString("page=2")));
        assertThat(links, not(hasItem(containsString("rel=\"prev\""))));
        assertThat(links, not(hasItem(containsString("rel=\"createKPIAssignment\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIAssignment\"")));
    }

    @Test
    @Order(2)
    void getAll_pageTwo_hasPrev() {
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(5))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getProject\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"prev\"")));
        assertThat(userLinks, hasItem(containsString("page=1")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"next\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createKPIAssignment\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIAssignment\"")));
    }

    @Test
    @Order(3)
    void getAll_hasFilter_returnsMatchingAndLinks() {
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("KPIId", KPI_ALPHA_ID)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .header("X-Total-Count", is("1"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getProject\"")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createKPIAssignment\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("KPIId", KPI_ALPHA_ID)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .header("X-Total-Count", is("1"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIAssignment\"")));
    }

    @Test
    @Order(4)
    void getAll_filtersNotMatching_returnsEmptyAndLinks() {
        String randomKPIId = UUID.randomUUID().toString();
        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("KPIId", randomKPIId)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("0"));

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("KPIId", randomKPIId)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("0"));

        List<String> emptyPageLinksUser = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 3)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .extract().headers().getValues("Link");

        assertThat(emptyPageLinksUser, hasItem(containsString("rel=\"getProject\"")));
        assertThat(emptyPageLinksUser, hasItem(containsString("rel=\"prev\"")));
        assertThat(emptyPageLinksUser, hasItem(containsString("page=2")));

        List<String> emptyPageLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 3)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .extract().headers().getValues("Link");

        assertThat(emptyPageLinks, hasItem(containsString("rel=\"createKPIAssignment\"")));
        assertThat(emptyPageLinksUser, hasItem(containsString("rel=\"getAllKPIItems\"")));

    }

    @Test
    @Order(5)
    void createKPIAssignment_bothAuths_matchingOutcome() {
        KPI_ALPHA_ID = createBaseKPI("Test KPI");
        String assignmentJson = String.format("{\"kpiId\": \"%s\", \"green\": 60.0, \"yellow\": 40.0, \"targetValue\": null}", KPI_ALPHA_ID);

        Response response = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(assignmentJson)
                .when()
                .post("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().response();

        String location = response.getHeader("Location");
        ASSIGNMENT_ID = location.substring(location.lastIndexOf("/") + 1);

        List<String> links = response.getHeaders().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllKPIAssignmentItems\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(assignmentJson)
                .when()
                .post("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(6)
    void getById_bothAuths_matchingOutcome() {
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");


        assertThat(userLinks, not(hasItem(containsString("rel=\"updateKPIAssignment\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"deleteKPIAssignment\""))));
        assertThat(userLinks, hasItem(containsString("rel=\"self\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"evaluateKPIAssignment\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIAssignmentItems\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIEntryItems\"")));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(200)
                .body("id", is(ASSIGNMENT_ID))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"updateKPIAssignment\"")));
        assertThat(adminLinks, hasItem(containsString("rel=\"deleteKPIAssignment\"")));
        assertThat(adminLinks, hasItem(containsString("rel=\"self\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"evaluateKPIAssignment\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIAssignmentItems\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIEntryItems\"")));
    }

    @Test
    @Order(7)
    void getById_isConditional_returnsCache() {
        String initialEtag = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().header("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-None-Match", initialEtag)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(304)
                .body(is(emptyOrNullString()));

        String updateJson = String.format(
                "{\"id\": \"%s\", \"kpiId\": \"%s\", \"projectId\": \"%s\", \"green\": 65.0, \"yellow\": 55.0}",
                ASSIGNMENT_ID, KPI_ALPHA_ID, PROJECT_ID
        );

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", initialEtag)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-None-Match", initialEtag)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(200)
                .header("ETag", not(equalTo(initialEtag)))
                .body("green", is(65.0f));
    }

    @Test
    @Order(8)
    void getAll_isConditional_returnsCache() {
        String initialEtag = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().header("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", initialEtag)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(304)
                .body(is(emptyOrNullString()));

        String updateJson = String.format(
                "{\"id\": \"%s\", \"kpiId\": \"%s\", \"projectId\": \"%s\", \"green\": 50.0, \"yellow\": 30.0}",
                ASSIGNMENT_ID, KPI_ALPHA_ID, PROJECT_ID
        );

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-None-Match", initialEtag)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments")
                .then()
                .statusCode(200)
                .header("ETag", not(equalTo(initialEtag)));
    }

    @Test
    @Order(9)
    void update_BothAuths_updatesAccording() {
        String updateJson = String.format(
                "{\"id\": \"%s\", \"kpiId\": \"%s\", \"projectId\": \"%s\", \"green\": 60.0, \"yellow\": 30.0}",
                ASSIGNMENT_ID, KPI_ALPHA_ID, PROJECT_ID
        );
        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(204)
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllKPIAssignmentItems\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @Order(10)
    void update_isConditional_updatesAccording() {
        String updateJson1 = String.format(
                "{\"id\": \"%s\", \"kpiId\": \"%s\", \"projectId\": \"%s\", \"green\": 50.0, \"yellow\": 30.0}",
                ASSIGNMENT_ID, KPI_ALPHA_ID, PROJECT_ID
        );
        String updateJson2 = String.format(
                "{\"id\": \"%s\", \"kpiId\": \"%s\", \"projectId\": \"%s\", \"green\": 60.0, \"yellow\": 30.0}",
                ASSIGNMENT_ID, KPI_ALPHA_ID, PROJECT_ID
        );

        String initialEtag = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .extract().header("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", initialEtag)
                .contentType(ContentType.JSON)
                .body(updateJson1)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", initialEtag)
                .contentType(ContentType.JSON)
                .body(updateJson2)
                .when()
                .put("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(412);
    }

    @Test
    @Order(11)
    void delete_bothAuths_deletesAccording() {
        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(403);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID)
                .then()
                .statusCode(404);
    }

    @Test
        @Order(12)
        void evaluate_noConditions_returnsCorrect() {
            String kpiId = createBaseKPI("Single Assignment Test");

            String assignmentId = createAssignmentReturnId(kpiId);

            createEntry(PROJECT_ID, assignmentId, 60.0);

            given()
                    .auth().oauth2(USER_TOKEN)
                    .when()
                    .get("/projects/" + PROJECT_ID + "/assignments/" + assignmentId + "/evaluation")
                    .then()
                    .statusCode(200)
                    .contentType(ContentType.JSON)
                    .body("status", anyOf(is("YELLOW"), is("Yellow")));
        }

        private static String createBaseKPI(String name) {
            Response res = given()
                    .auth().oauth2(ADMIN_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(String.format("{\"name\": \"%s\", \"destination\": \"Increasing\"}", name))
                    .post("/kpis");
            return res.getHeader("Location").substring(res.getHeader("Location").lastIndexOf("/") + 1);
        }

        private static void createAssignment(String kpiId) {
            String json = String.format("{\"kpiId\": \"%s\", \"green\": 100.0, \"yellow\": 60.0, \"targetValue\": null}", kpiId);
            given().auth().oauth2(ADMIN_TOKEN).contentType(ContentType.JSON).body(json)
                    .post("/projects/" + PROJECT_ID + "/assignments");
        }

        private static String createAssignmentReturnId(String kpiId) {
            String json = String.format("{\"kpiId\": \"%s\", \"green\": 100.0, \"yellow\": 60.0, \"targetValue\": null}", kpiId);
            Response res = given()
                    .auth().oauth2(ADMIN_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(json)
                    .post("/projects/" + PROJECT_ID + "/assignments")
                    .then()
                    .statusCode(201)
                    .extract().response();

            String loc = res.getHeader("Location");
            return loc.substring(loc.lastIndexOf("/") + 1);
        }

        private void createEntry(String pId, String aId, double value) {
            String json = String.format(java.util.Locale.US, "{\"measurement\": %f}", value);

            given()
                    .auth().oauth2(ADMIN_TOKEN)
                    .contentType(ContentType.JSON)
                    .body(json)
                    .when()
                    .post("/projects/" + pId + "/assignments/" + aId + "/entries")
                    .then()
                    .statusCode(201);
        }

    private static String registerUser(String user, String pw, String role) {
        String location = given()
                .contentType(ContentType.JSON)
                .body(String.format("{\"username\":\"%s\",\"password\":\"%s\",\"role\":\"%s\"}", user, pw, role))
                .post("/users")
                .then()
                .statusCode(201)
                .extract()
                .header("Location");

        return location.substring(location.lastIndexOf("/") + 1);
    }

        private static String fetchToken(String u, String p) {
            String b64 = Base64.getEncoder().encodeToString((u + ":" + p).getBytes());
        return given().header("Authorization", "Basic " + b64).contentType(ContentType.JSON)
                .post("/auth/login").then().extract().path("accessToken");
    }
}
