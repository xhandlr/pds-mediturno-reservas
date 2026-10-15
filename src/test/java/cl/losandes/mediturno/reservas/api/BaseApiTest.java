package cl.losandes.mediturno.reservas.api;

import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Base de las pruebas de API. No guarda estado compartido: cada test arma su propia
 * especificación con el reloj simulado que necesite (cabecera X-Reloj-Simulado), de modo que
 * las clases pueden correr en paralelo y sin depender del orden.
 */
public abstract class BaseApiTest {

    public static final String BASE_URL =
            System.getProperty("api.baseUrl", "http://localhost:8082");

    /** Reloj simulado de referencia para todos los escenarios. */
    public static final LocalDateTime AHORA = LocalDateTime.of(2026, 10, 5, 9, 0);

    /** Petición JSON contra el servicio, con el reloj simulado dado (obligatorio). */
    public static RequestSpecification spec(LocalDateTime reloj) {
        return RestAssured.given().spec(new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addHeader("X-Reloj-Simulado", reloj.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                .build());
    }

    /** Verifica estado HTTP y codigo del error; nunca mira el mensaje. */
    public static void assertError(Response respuesta, int httpStatus, String codigo) {
        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(httpStatus);
        assertThat(respuesta.jsonPath().getString("codigo")).as("codigo de error").isEqualTo(codigo);
    }

    /** Valida el cuerpo contra contratos/error.schema.json. */
    public static void assertEsquemaError(Response respuesta) {
        assertEsquema(respuesta, "error.schema.json");
    }

    /** Valida el cuerpo contra un esquema de src/test/resources/contratos/ (ej. "reserva.schema.json"). */
    public static void assertEsquema(Response respuesta, String archivo) {
        respuesta.then().assertThat()
                .body(JsonSchemaValidator.matchesJsonSchemaInClasspath("contratos/" + archivo));
    }
}
