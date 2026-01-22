package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Base64;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProjectResourceIT {

    private static String ADMIN_TOKEN;
    private static String ADMIN_ID;
    private static String USER_TOKEN;
    private static String PROJECT_ID;
    private static final String BASE_URL = "http://localhost:8080/api";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;

        ADMIN_ID = registerUser("admin-boss", "adminPass", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-boss", "adminPass");

        registerUser("normal-worker", "workerPass", "Member");
        USER_TOKEN = fetchToken("normal-worker", "workerPass");

        for (int i = 1; i <= 10; i++) {
            createProject("Alpha Project " + i, "https://github.com/alpha/" + i);
        }

        for (int i = 1; i <= 5; i++) {
            createProject("Beta Project " + i, "https://gitlab.com/beta/" + i);
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
                .get("/projects")
            .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"next\"")));
        assertThat(links, hasItem(containsString("page=2")));
        assertThat(links, not(hasItem(containsString("rel=\"prev\""))));
        assertThat(links, hasItem(containsString("rel=\"start\"")));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createProject\"")));
    }
    @Test
    @Order(2)
    void getAll_pageTwo_hasPrev() {
        List<String> links = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .body("size()", is(5))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"prev\"")));
        assertThat(links, hasItem(containsString("page=1")));
        assertThat(links, not(hasItem(containsString("rel=\"next\""))));
        assertThat(links, hasItem(containsString("rel=\"start\"")));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createProject\"")));
    }

    @Test
    @Order(3)
    void getAll_hasFilter_returnsMatchingAndLinks() {
        String filterName = "Beta";

        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("name", filterName)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .body("size()", is(5))
                .header("X-Total-Count", is("5"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"start\"")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createProject\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"next\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("name", filterName)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createProject\"")));
    }

    @Test
    @Order(4)
    void getAll_filtersNotMatching_returnsEmptyAndLinks() {

        List<String> gammaLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("name", "Gamma")
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("0"))
                .extract().headers().getValues("Link");

        assertThat(gammaLinks, hasItem(containsString("rel=\"start\"")));
        assertThat(gammaLinks, not(hasItem(containsString("rel=\"next\""))));
        assertThat(gammaLinks, not(hasItem(containsString("rel=\"prev\""))));

        List<String> pageThreeLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 3)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .body("size()", is(0))
                .extract().headers().getValues("Link");

        assertThat(pageThreeLinks, hasItem(containsString("rel=\"start\"")));
        assertThat(pageThreeLinks, hasItem(containsString("rel=\"prev\"")));
        assertThat(pageThreeLinks, hasItem(containsString("page=2")));
        assertThat(pageThreeLinks, not(hasItem(containsString("rel=\"next\""))));
    }

    @Test
    @Order(5)
    void createProject_bothAuths_matchingOutcome() {
        String projectJson = "{\"name\": \"Integration Project\", \"repoUrl\": \"https://github.com/test/repo\"}";

        Response response = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(projectJson)
            .when()
                .post("/projects")
            .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().response();

        String location = response.getHeader("Location");
        PROJECT_ID = location.substring(location.lastIndexOf("/") + 1);

        List<String> links = response.getHeaders().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllProjectItems\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(projectJson)
            .when()
                .post("/projects")
            .then()
                .statusCode(403);
    }

    @Test
    @Order(6)
    void getAll_isConditional_returnsCache() {
        Response response = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().response();

        String etag = response.getHeader("ETag");

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", etag)
            .when()
                .get("/projects")
            .then()
                .statusCode(304)
                .body(is(emptyOrNullString()));

        String updatedJson = String.format("{\"id\": \"" + PROJECT_ID + "\", \"name\": \"%s\", \"repoUrl\": \"https://github.com/updated\"}", "Integration Project Update");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updatedJson)
            .when()
                .put("/projects/" + PROJECT_ID)
            .then()
                .statusCode(204);

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", etag)
            .when()
                .get("/projects")
            .then()
                .statusCode(200)
                .header("ETag", not(is(etag)));
    }

    @Test
    @Order(7)
    void getById_bothAuths_returnsMatchingLinks() {

        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID)
                .then()
                .statusCode(200)
                .body("id", is(PROJECT_ID))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getAllProjectItems\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIAssignmentItems\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"evaluateProject\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"self\"")));

        assertThat(userLinks, not(hasItem(containsString("rel=\"updateProject\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"deleteProject\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID)
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"updateProject\"")));
        assertThat(adminLinks, hasItem(containsString("rel=\"deleteProject\"")));
    }

    @Test
    @Order(8)
    void evaluate_noConditions_returnsCorrect(){
        String kpiId = createKPI("Eval KPI");
        String assignmentId = createAssignment(PROJECT_ID, kpiId);
        createEntry(PROJECT_ID, assignmentId);

        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/evaluation")
                .then()
                .statusCode(200)
                .body("project.id", is(PROJECT_ID))
                .body("status", is("RED"))
                .body("allKpis", hasSize(greaterThan(0)))
                .body("allKpis[0].status", is("RED"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getProject\"")));
    }

    @Test
    @Order(9)
    void getById_isConditional_returnCache() {
        Response response = given()
                .auth().oauth2(USER_TOKEN)
            .when()
                .get("/projects/" + PROJECT_ID)
            .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().response();

        String etag = response.getHeader("ETag");

        given()
                .auth().oauth2(USER_TOKEN)
                .header("If-None-Match", etag)
            .when()
                .get("/projects/" + PROJECT_ID)
            .then()
                .statusCode(304)
                .body(is(emptyOrNullString()));

        given()
                .auth().oauth2(USER_TOKEN)
                .header("If-None-Match", "\"wrong-etag-123\"")
            .when()
                .get("/projects/" + PROJECT_ID)
            .then()
                .statusCode(200)
                .body("id", is(PROJECT_ID));
    }

    @Test
    @Order(10)
    void update_BothAuths_updatesAccording() {
        String updateJson = "{\"id\": \"" + PROJECT_ID + "\", \"name\": \"Updated Name\", \"repoUrl\": \"https://github.com/test/repo\"}";

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
            .when()
                .put("/projects/" + PROJECT_ID)
            .then()
                .statusCode(204)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"self\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
            .when()
                .put("/projects/" + PROJECT_ID)
            .then()
                .statusCode(403);
    }

    @Test
    @Order(11)
    void update_isConditional_updatesAccording() {
        String initialEtag = given()
                .auth().oauth2(ADMIN_TOKEN)
            .when()
                .get("/projects/" + PROJECT_ID)
            .then()
                .statusCode(200)
                .extract().header("ETag");

        String firstUpdate = "{\"id\": \"" + PROJECT_ID + "\", \"name\": \"First Update\", \"repoUrl\": \"https://github.com/test/repo\"}";
        String secondUpdate = "{\"id\": \"" + PROJECT_ID + "\", \"name\": \"Second Update\", \"repoUrl\": \"https://github.com/test/repo\"}";

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", initialEtag)
                .contentType(ContentType.JSON)
                .body(firstUpdate)
            .when()
                .put("/projects/" + PROJECT_ID)
            .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", initialEtag)
                .contentType(ContentType.JSON)
                .body(secondUpdate)
            .when()
                .put("/projects/" + PROJECT_ID)
            .then()
                .statusCode(412);
    }

    @Test
    @Order(12)
    void delete_bothAuths_deletesAccording() {
        given()
                .auth().oauth2(USER_TOKEN)
            .when()
                .delete("/projects/" + PROJECT_ID)
            .then()
                .statusCode(403);

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
            .when()
                .delete("/projects/" + PROJECT_ID)
            .then()
                .statusCode(204)
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"getAllProjectItems\"")));

        given()
                .auth().oauth2(ADMIN_TOKEN)
            .when()
                .delete("/projects/" + PROJECT_ID)
            .then()
                .statusCode(404);
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

    private static String fetchToken(String user, String pass) {
        String b64 = Base64.getEncoder().encodeToString((user + ":" + pass).getBytes());

        return given()
                .header("Authorization", "Basic " + b64)
                .contentType(ContentType.JSON)
                .when()
                .post("/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("accessToken");
    }

    private static void createProject(String name, String repoUrl) {
        String json = String.format("{\"name\": \"%s\", \"repoUrl\": \"%s\"}", name, repoUrl);
        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .post("/projects")
            .then()
                .statusCode(201);
    }

    private String createKPI(String name) {
        String json = String.format("{\"name\": \"%s\", \"destination\": \"Increasing\"}", name);
        Response res = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .post("/kpis");
        return res.getHeader("Location").substring(res.getHeader("Location").lastIndexOf("/") + 1);
    }

    private String createAssignment(String pId, String kId) {
        String json = String.format(
                "{\"kpiId\": \"%s\", \"green\": 80.0, \"yellow\": 50.0}",
                kId
        );
        Response res = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .post("/projects/" + pId + "/assignments");
        return res.getHeader("Location").substring(res.getHeader("Location").lastIndexOf("/") + 1);
    }

    private void createEntry(String pId, String aId) {
        String json = String.format("{\"measurement\": 10.0}");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/projects/" + pId + "/assignments/" + aId + "/entries")
                .then()
                .statusCode(201);
    }
}