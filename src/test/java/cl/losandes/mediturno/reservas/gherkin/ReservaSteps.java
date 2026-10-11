package cl.losandes.mediturno.reservas.gherkin;

import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import io.restassured.http.ContentType;
import io.restassured.response.Response;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

public class ReservaSteps {

    private static final String RELOJ = "2026-10-05T09:00:00";

    private String rutPaciente;
    private String canal;
    private LocalDateTime fechaAtencion;
    private Response respuesta;

    @Dado("un paciente con previsión {word}")
    public void unPacienteConPrevision(String particionPrevision) {

        rutPaciente = "T-" +
                UUID.randomUUID()
                        .toString()
                        .substring(0, 8);

        Map<String, Object> paciente = new LinkedHashMap<>();

        paciente.put("rut", rutPaciente);
        paciente.put("nombre", "Paciente Cucumber");
        paciente.put("fechaNacimiento", "1990-01-01");
        paciente.put("convenioEmpresa", false);

        switch (particionPrevision) {

            case "FONASA_A" -> {
                paciente.put("prevision", "FONASA");
                paciente.put("tramoFonasa", "A");
            }

            case "FONASA_BD" -> {
                paciente.put("prevision", "FONASA");
                paciente.put("tramoFonasa", "B");
            }

            case "ISAPRE" -> {
                paciente.put("prevision", "ISAPRE");
                paciente.put("tramoFonasa", null);
            }

            case "PARTICULAR" -> {
                paciente.put("prevision", "PARTICULAR");
                paciente.put("tramoFonasa", null);
            }

            default -> throw new IllegalArgumentException(
                    "Previsión no soportada: " + particionPrevision
            );
        }

        given()
                .contentType(ContentType.JSON)
                .body(paciente)
        .when()
                .post("/api/pacientes")
        .then()
                .statusCode(201);
    }


    @Dado("que solicita una atención con anticipación {word}")
    public void solicitaAtencionConAnticipacion(String anticipacion) {

        LocalDateTime ahora = LocalDateTime.parse(RELOJ);

        fechaAtencion = switch (anticipacion) {

            case "PASADO_O_AHORA" ->
                    ahora.minusMinutes(20);

            case "MENOR_60_MIN" ->
                    ahora.plusMinutes(40);

            case "60_MIN" ->
                    ahora.plusMinutes(60);

            case "61_239_MIN" ->
                    ahora.plusMinutes(120);

            case "240_MIN" ->
                    ahora.plusMinutes(240);

            case "MAYOR_240_MIN_MENOR_60D" ->
                    ahora.plusDays(7);

            case "60_DIAS" ->
                    ahora.plusDays(60);

            case "MAYOR_60_DIAS" ->
                    ahora.plusDays(62);

            default -> throw new IllegalArgumentException(
                    "Anticipación no soportada: " + anticipacion
            );
        };
    }


    @Dado("utiliza el canal {word}")
    public void utilizaElCanal(String canal) {
        this.canal = canal;
    }


    @Cuando("intenta crear la reserva")
    public void intentaCrearLaReserva() {

        Map<String, Object> reserva = new LinkedHashMap<>();

        reserva.put("rutPaciente", rutPaciente);
        reserva.put("especialidad", "MEDICINA_GENERAL");
        reserva.put("fechaHoraAtencion", fechaAtencion.toString());
        reserva.put("canal", canal);
        reserva.put("aceptaListaEspera", false);

        respuesta = given()
                .header(
                        "X-Reloj-Simulado",
                        RELOJ
                )
                .contentType(ContentType.JSON)
                .body(reserva)
        .when()
                .post("/api/reservas");
    }


    @Entonces("la reserva debe ser rechazada por no encontrarse en el futuro")
    public void reservaRechazadaPorEstarEnElPasado() {

        respuesta.then()
                .statusCode(400)
                .body(
                        "codigo",
                        equalTo("RES-02-PASADO")
                );
    }
}
