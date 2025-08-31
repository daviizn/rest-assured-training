package dev.davizn.rest.tests;

import dev.davizn.rest.core.BaseTest;
import dev.davizn.rest.utils.DataUtils;
import io.restassured.RestAssured;
import io.restassured.specification.FilterableRequestSpecification;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BarrigaTest extends BaseTest {

    private static String CONTA_NAME = "Conta " + System.nanoTime();
    private static Integer CONTA_ID;
    private static Integer MOV_ID;

    @BeforeClass
    public static void login() {
        Map<String, String> login = new HashMap<>();
        login.put("email", "davizn@dev");
        login.put("senha", "123456");

        String TOKEN = given()
            .body(login)
        .when()
            .post("/signin")
        .then()
            .statusCode(200)
            .extract().path("token");

        RestAssured.requestSpecification.header("Authorization", "JWT" + TOKEN);
    }

    @Test
    public void CT02_deveIncluirContaComSucesso() {
        CONTA_ID = given()
            .body("{\"nome\": \""+CONTA_NAME+"\"}")
        .when()
            .post("/contas")
        .then()
            .statusCode(201)
            .extract().path("id")
        ;

    }

    @Test
    public void CT03_deveAlterarContaComSucesso() {
        given()
            .body("{\"nome\": \""+CONTA_NAME+" alterada\"}")
            .pathParam("id", CONTA_ID)
        .when()
            .put("/contas/{id}")
        .then()
            .statusCode(200)
            .body("nome", is(CONTA_NAME + " alterada"))
        ;

    }

    @Test
    public void CT04_naoDeveInserirContaMesmoNome() {
        given()
            .body("{\"nome\": \""+CONTA_NAME+" alterada\"}")
        .when()
            .post("/contas")
        .then()
            .statusCode(400)
            .body("error", is("Já existe uma conta com esse nome!"))
        ;

    }

    @Test
    public void CT05_deveInserirMovimentacaoComSucesso() {
        Movimentacao mov =  getMovimentacaoValida();

        MOV_ID = given()
            .body(mov)
        .when()
            .post("/transacoes")
        .then()
            .statusCode(201)
            .extract().path("id")
        ;

    }

    @Test
    public void CT06_deveValidarCamposObrigatoriosMovimentacao() {
        given()
            .body("{}")
        .when()
            .post("/transacoes")
        .then()
            .statusCode(400)
            .body("$", hasSize(8))
            .body("msg", hasItems(
                    "Data da Movimentação é obrigatório",
                    "Data do pagamento é obrigatório",
                    "Descrição é obrigatório",
                    "Interessado é obrigatório",
                    "Valor é obrigatório",
                    "Valor deve ser um número",
                    "Conta é obrigatório",
                    "Situação é obrigatório"
                ))
        ;

    }

    @Test
    public void CT07_naoDeveInserirMovimentacaoComDataFutura() {
        Movimentacao mov = getMovimentacaoValida();
        mov.setData_transacao(DataUtils.getDataDiferencaDias(2));

        given()
            .body(mov)
        .when()
            .post("/transacoes")
        .then()
            .statusCode(400)
            .body("$", hasSize(1))
            .body("msg", hasItems("Data da Movimentação deve ser menor ou igual à data atual"))
        ;

    }

    @Test
    public void CT08_naoDeveRemoverContaComMovimentacao() {
        given()
            .pathParam("id", CONTA_ID)
        .when()
            .delete("/contas/{id}")
        .then()
            .statusCode(500)
            .body("constraint", is("transacoes_conta_id_foreign"))
        ;

    }

    @Test
    public void CT09_deveCalcularSaldoContas() {
        given()
        .when()
            .get("/saldo")
        .then()
            .statusCode(200)
            .body("find{it.conta_id == "+CONTA_ID+"}.saldo", is("100.00"))
        ;

    }

    @Test
    public void CT10_deveRemoverMovimentacao() {
        given()
            .pathParam("id", MOV_ID)
        .when()
            .delete("/transacoes/{id}")
        .then()
            .statusCode(204)

        ;

    }

    @Test
    public void CT11_naoDeveAcessarAPISemToken() {
        FilterableRequestSpecification req = (FilterableRequestSpecification) RestAssured.requestSpecification;
        req.removeHeader("Authorization");

        given()
        .when()
            .get("/contas")
        .then()
            .statusCode(401)
        ;
    }

    private Movimentacao getMovimentacaoValida() {
        Movimentacao mov =  new Movimentacao();
        mov.setConta_id(CONTA_ID);
        mov.setDescricao("Descricao de movimentacao");
        mov.setEnvolvido("Envolvido na mov");
        mov.setTipo("REC");
        mov.setData_transacao(DataUtils.getDataDiferencaDias(-1));
        mov.setData_pagamento(DataUtils.getDataDiferencaDias(5));
        mov.setValor(100f);
        mov.setStatus(true);
        return mov;
    }
}
