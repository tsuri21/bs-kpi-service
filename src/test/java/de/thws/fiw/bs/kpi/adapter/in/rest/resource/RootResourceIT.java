package de.thws.fiw.bs.kpi.adapter.in.rest.resource;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Base64;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;

class RootResourceIT {

    private static String USER_TOKEN;
    private static String ADMIN_TOKEN;
    private static String ADMIN_ID;

    @BeforeAll
    static void setup() {

        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8080;
        RestAssured.basePath = "/api";
        ADMIN_ID= registerUser("admin-boss", "adminPass", "ADMIN");
        ADMIN_TOKEN = fetchToken("admin-boss", "adminPass");

        registerUser("normal-worker", "workerPass", "Member");
        USER_TOKEN = fetchToken("normal-worker", "workerPass");
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

    @Test
    void testRoot_Anonymous_ShouldSeeLoginAndRegister() {
        List<String> links = given()
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"register\"")));
        assertThat(links, hasItem(containsString("rel=\"login\"")));
        assertThat(links, not(hasItem(containsString("rel=\"getAllKPIItems\""))));
        assertThat(links, not(hasItem(containsString("rel=\"getAllProjectItems\""))));
    }

    @Test
    void testRoot_User_ShouldSeeProjectsAndMe() {
        List<String> links = given()
                .header("Authorization", "Bearer " + USER_TOKEN)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"getCurrentUser\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllProjectItems\"")));
        assertThat(links, not(hasItem(containsString("rel=\"getAllKPIItems\""))));
    }

    @Test
    void testRoot_Admin_ShouldSeeEverything() {
        List<String> links = given()
                .header("Authorization", "Bearer " + ADMIN_TOKEN)
                .when()
                .get("/")
                .then()
                .statusCode(200)
                .extract().headers().getValues("Link");

        assertThat(links, hasItem(containsString("rel=\"getCurrentUser\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllProjectItems\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllKPIItems\"")));
        assertThat(links, hasItem(containsString("rel=\"getAllUserItems\"")));
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
}