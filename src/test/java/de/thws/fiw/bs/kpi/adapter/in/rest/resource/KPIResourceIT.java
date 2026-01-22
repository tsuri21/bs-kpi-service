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
class KPIResourceIT {
    private static String ADMIN_TOKEN;
    private static String ADMIN_ID;
    private static String USER_TOKEN;
    private static String KPI_ID;
    private static final String BASE_URL = "http://localhost:8080/api";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;

        ADMIN_ID= registerUser("admin-boss", "adminPass", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-boss", "adminPass");

        registerUser("normal-worker", "workerPass", "Member");
        USER_TOKEN = fetchToken("normal-worker", "workerPass");

        for (int i = 1; i <= 10; i++) {
            createKPI("Alpha KPI " + i, "Increasing");
        }

        for (int i = 1; i <= 10; i++) {
            createKPI("Beta KPI " + i, "Decreasing");
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
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .body("size()", is(15))
                .header("X-Total-Count", is("20"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"createKPI\"")));
        assertThat(links, hasItem(containsString("rel=\"next\"")));
        assertThat(links, hasItem(containsString("page=2")));
        assertThat(links, not(hasItem(containsString("rel=\"prev\""))));

        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/kpis")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(2)
    void getAll_pageTwo_hasPrev() {
        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .body("size()", is(5))
                .header("X-Total-Count", is("20"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"createKPI\"")));
        assertThat(links, hasItem(containsString("rel=\"prev\"")));
        assertThat(links, hasItem(containsString("page=1")));
        assertThat(links, not(hasItem(containsString("rel=\"next\""))));

        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/kpis")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(3)
    void getAll_hasFilter_returnsMatchingAndLinks() {
        String filterName = "Alpha";

        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("name", filterName)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("10"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"createKPI\"")));
        assertThat(links, not(hasItem(containsString("rel=\"next\""))));

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("name", filterName)
                .when()
                .get("/kpis")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(4)
    void getAll_filtersNotMatching_returnsEmptyAndLinks() {
        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("name", "Gamma")
                .when()
                .get("/kpis")
                .then()
                .statusCode(403);

        List<String> emptyPageLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 3)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .extract().headers().getValues("Link");

        assertThat(emptyPageLinks, hasItem(containsString("rel=\"start\"")));
        assertThat(emptyPageLinks, hasItem(containsString("rel=\"createKPI\"")));
        assertThat(emptyPageLinks, hasItem(containsString("rel=\"prev\"")));
        assertThat(emptyPageLinks, hasItem(containsString("page=2")));

    }

    @Test
    @Order(5)
    void createKPI_bothAuths_matchingOutcome() {
        String kpiJson = "{\"name\": \"Integration KPI\", \"destination\": \"Increasing\"}";

        Response response = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(kpiJson)
                .when()
                .post("/kpis")
                .then()
                .statusCode(201)
                .header("Location", notNullValue())
                .extract().response();

        String location = response.getHeader("Location");
        KPI_ID = location.substring(location.lastIndexOf("/") + 1);

        List<String> links = response.getHeaders().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllKPIItems\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(kpiJson)
                .when()
                .post("/kpis")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(6)
    void getAll_isConditional_returnsCache() {
        Response response = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().response();

        String etag = response.getHeader("ETag");

        String updatedJson = String.format(
                "{\"id\": \"%s\", \"name\": \"ETag Update Test\"}",
                KPI_ID
        );

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updatedJson)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", etag)
                .when()
                .get("/kpis")
                .then()
                .statusCode(200)
                .header("ETag", not(is(etag)));
    }
    @Test
    @Order(7)
    void getById_bothAuths_returnsMatchingLinks() {

        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .statusCode(200)
                .body("id", is(KPI_ID))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"getAllKPIItems\"")));
        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"updateKPI\"")));
        assertThat(links, hasItem(containsString("rel=\"deleteKPI\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .statusCode(403);
    }
    @Test
    @Order(8)
    void getById_isConditional_returnsCache() {
        Response response = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .statusCode(200)
                .header("ETag", notNullValue())
                .extract().response();

        String etag = response.getHeader("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-None-Match", etag)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .statusCode(304);

        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @Order(9)
    void update_BothAuths_updatesAccording() {
        String updateJson = String.format("{\"id\": \"%s\", \"name\": \"Updated KPI Name\"}", KPI_ID);

        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(204)
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"self\"")));

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @Order(10)
    void update_isConditional_updatesAccording() {
        String updateJson1 = String.format("{\"id\": \"%s\", \"name\": \"First Update\"}", KPI_ID);
        String updateJson2 = String.format("{\"id\": \"%s\", \"name\": \"Second Update\"}", KPI_ID);

        String etag = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
                .then()
                .extract().header("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", etag)
                .contentType(ContentType.JSON)
                .body(updateJson1)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .header("If-Match", etag) // Alter ETag
                .contentType(ContentType.JSON)
                .body(updateJson2)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(412);

        given()
                .auth().oauth2(USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(updateJson2)
                .when()
                .patch("/kpis/" + KPI_ID)
                .then()
                .statusCode(403);
    }

    @Test
    @Order(11)
    void delete_bothAuths_deletesAccording() {
        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .delete("/kpis/" + KPI_ID)
                .then()
                .statusCode(403);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .delete("/kpis/" + KPI_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/kpis/" + KPI_ID)
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

    private static void createKPI(String name, String destination) {
        String json = String.format("{\"name\": \"%s\", \"destination\": \"%s\"}", name, destination);
        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/kpis")
                .then()
                .statusCode(201);
    }
}