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
public class UserResourceIT {

    private static String ADMIN_TOKEN;
    private static String USER_TOKEN;
    private static String ADMIN_ID;
    private static String RANDOM_USER_ID;

    @BeforeAll
    static void setup() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api";

        ADMIN_ID = registerUser("admin-boss", "admin-pw", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-boss", "admin-pw");

        registerUser("normal-worker", "user-pw", "Member");
        USER_TOKEN = fetchToken("normal-worker", "user-pw");

        for (int i = 0; i < 15; i++) {
            if (i == 0) {
                RANDOM_USER_ID = registerUser("pagination_user_" + i, "pw", "Member");
            } else {
                registerUser("pagination_user_" + i, "pw", "Member");
            }
        }
    }

    @AfterAll
    static void tearDown() {

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

    @Test
    @Order(1)
    void getAll_pageOne_hasNext() {
        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 1)
                .when()
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", is(10))
                .header("X-Total-Count", is("17"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"next\"")));
        assertThat(links, hasItem(containsString("page=2")));
        assertThat(links, not(hasItem(containsString("rel=\"prev\""))));

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 1)
                .when()
                .get("/users")
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
                .get("/users")
                .then()
                .statusCode(200)
                .body("size()", is(7))
                .header("X-Total-Count", is("17"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"prev\"")));
        assertThat(links, hasItem(containsString("page=1")));
        assertThat(links, not(hasItem(containsString("rel=\"next\""))));

        given()
                .auth().oauth2(USER_TOKEN)
                .queryParam("page", 2)
                .when()
                .get("/users")
                .then()
                .statusCode(403);
    }

    @Test
    @Order(3)
    void getAll_isConditional_returnsCache() {
        String initialListEtag = given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .get("/users")
                .then().statusCode(200)
                .extract().header("ETag");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", initialListEtag)
                .get("/users")
                .then()
                .statusCode(304)
                .header("ETag", is(initialListEtag));

        String id = registerUser("testworker", "user-pw", "Member");

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .queryParam("page", 2)
                .header("If-None-Match", initialListEtag)
                .get("/users")
                .then()
                .statusCode(200)
                .header("ETag", not(is(initialListEtag)));

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .delete("/users/" + id)
                .then().statusCode(204);
    }

    @Test
    @Order(4)
    void getMe_User_Success() {
        List<String> links = given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("username", is("normal-worker"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"deleteUser\"")));
        assertThat(links, not(hasItem(containsString("rel=\"getAllUserItem\""))));

        List<String> adminLinks = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("username", is("admin-boss"))
                .extract().headers().getValues("Link");

        assertThat(adminLinks, hasItem(containsString("rel=\"start\"")));
        assertThat(adminLinks, hasItem(containsString("rel=\"self\"")));
        assertThat(adminLinks, hasItem(containsString("rel=\"deleteUser\"")));
    }

    @Test
    @Order(5)
    void getById_bothAuths_returnsMatchingLinks() {
        given()
                .auth().oauth2(USER_TOKEN)
                .when()
                .get("/users/" + RANDOM_USER_ID)
                .then()
                .statusCode(403);

        List<String> links = given()
                .auth().oauth2(ADMIN_TOKEN)
                .when()
                .get("/users/me")
                .then()
                .statusCode(200)
                .body("username", is("admin-boss"))
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"start\"")));
        assertThat(links, hasItem(containsString("rel=\"self\"")));
        assertThat(links, hasItem(containsString("rel=\"deleteUser\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllUserItems\"")));
    }

    @Test
    @Order(6)
    void delete_bothAuths_deletesAccording() {
        given()
                .auth().oauth2(USER_TOKEN)
                .delete("/users/" + RANDOM_USER_ID)
                .then()
                .statusCode(403);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .delete("/users/" + RANDOM_USER_ID)
                .then()
                .statusCode(204);

        given()
                .auth().oauth2(ADMIN_TOKEN)
                .delete("/users/" + RANDOM_USER_ID)
                .then()
                .statusCode(404);
    }
}