package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.Base64;
import java.util.List;
import java.util.Locale;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class KPIEntryResourceIT {

    private static String ADMIN_TOKEN;
    private static String ADMIN_ID;
    private static String USER_TOKEN;
    private static String TECH_USER_TOKEN;
    private static String PROJECT_ID;
    private static String ASSIGNMENT_ID;
    private static String KPI_ID;
    private static String ENTRY_ID_1;
    private static String ENTRY_ID_2;
    private static String FIRST_ENTRY;

    private static final String BASE_URL = "http://localhost:8080/api";

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = BASE_URL;

        ADMIN_ID = registerUser("admin-entry", "adminPass", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-entry", "adminPass");
        registerUser("user-entry", "userPass", "Member");
        USER_TOKEN = fetchToken("user-entry", "userPass");
        registerUser("techuser-entry", "techPass", "Technical_User");
        TECH_USER_TOKEN = fetchToken("techuser-entry", "techPass");

        Response pRes = given().auth().oauth2(ADMIN_TOKEN).contentType(ContentType.JSON)
                .body("{\"name\": \"Entry Test Project\", \"repoUrl\": \"https://github.com/user/repo\"}").post("/projects");
        PROJECT_ID = pRes.getHeader("Location").substring(pRes.getHeader("Location").lastIndexOf("/") + 1);

        Response kRes = given().auth().oauth2(ADMIN_TOKEN).contentType(ContentType.JSON)
                .body("{\"name\": \"Entry Test KPI\", \"destination\": \"Increasing\"}").post("/kpis");
        KPI_ID = kRes.getHeader("Location").substring(kRes.getHeader("Location").lastIndexOf("/") + 1);

        String assignJson = String.format("{\"kpiId\": \"%s\", \"green\": 80.0, \"yellow\": 50.0}", KPI_ID);
        Response aRes = given().auth().oauth2(ADMIN_TOKEN).contentType(ContentType.JSON).body(assignJson).post("/projects/" + PROJECT_ID + "/assignments");
        ASSIGNMENT_ID = aRes.getHeader("Location").substring(aRes.getHeader("Location").lastIndexOf("/") + 1);

        createEntry(10.0);

        Response res = given()
                .auth().oauth2(ADMIN_TOKEN)
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries");

        FIRST_ENTRY = res.jsonPath().getString("[0].timestamp");


        for (int i = 1; i < 15; i++) {
            createEntry(10.0 + i);
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
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"next\"")));
        assertThat(userLinks, hasItem(containsString("page=2")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"prev\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createKPIEntry\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIEntry\"")));

        List<String> techLinks = given()
                .auth().oauth2(TECH_USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(techLinks, hasItem(containsString("rel=\"createKPIEntry\"")));
    }

    @Test
    @Order(2)
    void getAll_pageTwo_hasPrev() {
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(5))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"prev\"")));
        assertThat(userLinks, hasItem(containsString("page=1")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"next\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createKPIEntry\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIEntry\"")));
    }

    @Test
    @Order(3)
    void getAll_hasFilter_returnsMatchingAndLinks() {
        String fromDate = java.time.Instant.parse(FIRST_ENTRY).minusMillis(1).toString();
        String toDate = java.time.Instant.parse(FIRST_ENTRY).plusMillis(1).toString();

        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("from", fromDate)
                .queryParam("to", toDate)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .header("X-Total-Count", is("1"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, not(hasItem(containsString("rel=\"next\""))));
        assertThat(userLinks, not(hasItem(containsString("rel=\"createKPIEntry\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("from", FIRST_ENTRY)
                .queryParam("to", toDate)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(1))
                .header("X-Total-Count", is("1"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIEntry\"")));
    }

    @Test
    @Order(4)
    void getAll_filtersNotMatching_returnsEmptyAndLinks() {
        String farFuture = "2099-01-01T00:00:00Z";
        String futureTo = "2099-01-02T00:00:00Z";

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("from", farFuture)
                .queryParam("to", futureTo)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("0"));

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("from", farFuture)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("0"));

        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 3)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getKPIAssignment\"")));
        assertThat(userLinks, hasItem(containsString("rel=\"prev\"")));
        assertThat(userLinks, hasItem(containsString("page=2")));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 3)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .body("size()", is(0))
                .header("X-Total-Count", is("15"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"createKPIEntry\"")));
    }

    @Test
    @Order(5)
    void getAll_isConditional_returnsCache() {
        String initialListEtag = given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then().statusCode(200)
                .extract().header("ETag");

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", initialListEtag)
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(304)
                .header("ETag", is(initialListEtag));

        String testJson = String.format(java.util.Locale.US, "{\"measurement\": %f}", 42.5);
        Response createRes = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(testJson)
                .post("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then().statusCode(201)
                .extract().response();

        String location = createRes.getHeader("Location");
        String tempEntryId = location.substring(location.lastIndexOf("/") + 1);

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", initialListEtag)
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(200)
                .header("ETag", not(is(initialListEtag)));

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + tempEntryId)
                .then().statusCode(204);
    }

    @Test
    @Order(6)
    void createKPIEntry_bothAuths_matchingOutcome() {
        String json = String.format("{\"measurement\": 99.0}");
        String json2 = String.format("{\"measurement\": 89.0}");

        Response res = given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(201)
                .extract().response();

        ENTRY_ID_1 = res.getHeader("Location").substring(res.getHeader("Location").lastIndexOf("/") + 1);
        List<String> links = res.getHeaders().getValues("Link");
        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllKPIEntryItems\"")));

        Response res2 = given()
                .auth().oauth2(TECH_USER_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .when()
                .post("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then()
                .statusCode(201)
                .extract().response();

        ENTRY_ID_2 = res2.getHeader("Location").substring(res2.getHeader("Location").lastIndexOf("/") + 1);
        List<String> links2 = res.getHeaders().getValues("Link");
        assertThat(links2, hasItem(containsString("rel=\"self\"")));
        assertThat(links2, hasItem(containsString("rel=\"getAllKPIEntryItems\"")));

        given().auth().oauth2(USER_TOKEN).contentType(ContentType.JSON).body(json)
                .post("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
                .then().statusCode(403);
    }

    @Test
    @Order(7)
    void getById_bothAuths_matchingOutcome() {
        List<String> userLinks = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_1)
                .then()
                .statusCode(200)
                .headers("Cache-Control", notNullValue())
                .extract().headers().getValues("Link");

        assertThat(userLinks, hasItem(containsString("rel=\"getAllKPIEntryItems\"")));
        assertThat(userLinks, not(hasItem(containsString("rel=\"deleteKPIEntry\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_1)
                .then()
                .statusCode(200)
                .headers("Cache-Control", notNullValue())
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"deleteKPIEntry\"")));

        List<String> techLinks = given()
                .auth().oauth2(TECH_USER_TOKEN)
                .when()
                .get("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/"  + ENTRY_ID_1)
                .then()
                .statusCode(200)
                .headers("Cache-Control", notNullValue())
                .extract().headers().getValues("Link");

        assertThat(techLinks, hasItem(containsString("rel=\"deleteKPIEntry\"")));
    }

    @Test
    @Order(8)
    void delete_bothAuths_deletesAccording() {
        given().auth().oauth2(USER_TOKEN).delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_1)
                .then().statusCode(403);

        given().auth().oauth2(ADMIN_TOKEN).delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_1)
                .then().statusCode(204);

        given().auth().oauth2(ADMIN_TOKEN).delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_1)
                .then().statusCode(404);

        given().auth().oauth2(ADMIN_TOKEN).delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_2)
                .then().statusCode(204);

        given().auth().oauth2(ADMIN_TOKEN).delete("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries/" + ENTRY_ID_2)
                .then().statusCode(404);
    }

    private static void createEntry(double val) {
        String json = String.format(Locale.US, "{\"measurement\": %f}", val);
        given()
                .auth().oauth2(ADMIN_TOKEN)
                .contentType(ContentType.JSON)
                .body(json)
                .post("/projects/" + PROJECT_ID + "/assignments/" + ASSIGNMENT_ID + "/entries")
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
        return given()
                .header("Authorization", "Basic " + b64)
                .contentType(ContentType.JSON)
                .post("/auth/login")
                .then()
                .extract().path("accessToken");
    }
}