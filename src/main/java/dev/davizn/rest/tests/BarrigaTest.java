package dev.davizn.rest.tests;

import dev.davizn.rest.core.BaseTest;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

public class BarrigaTest extends BaseTest {

    private String TOKEN;

    @Before
    public void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "davizn@dev");
        login.put("senha", "123456");

        TOKEN = given()
            .body(login)
        .when()
            .post("/signin")
        .then()
            .statusCode(200)
            .extract().path("token");
    }

    @Test
    public void naoDeveAcessarAPISemToken() {
        given()
        .when()
            .get("/contas")
        .then()
            .statusCode(401)
        ;
    }

    @Test
    public void deveIncluirContaComSucesso() {
        given()
            .header("Authorization", "JWT " + TOKEN)
            .body("{\"nome\": \"conta qualquer\"}")
        .when()
            .post("/contas")
        .then()
            .statusCode(201)
        ;

    }
}
