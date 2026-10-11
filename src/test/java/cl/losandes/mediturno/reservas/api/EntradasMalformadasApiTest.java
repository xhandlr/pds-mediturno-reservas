package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EntradasMalformadasApiTest extends BaseApiTest {

    @Test
    @DisplayName("CP-20: rechazar reserva sin rutPaciente obligatorio")
    void cp20_reservaSinRutPaciente() {
        String cuerpo = """
                {"especialidad":"MEDICINA_GENERAL",\
                "fechaHoraAtencion":"2026-10-06T10:00:00","canal":"WEB"}""";

        Response respuesta = spec(AHORA).body(cuerpo).post("/api/reservas");

        assertError(respuesta, 400, "VAL-CAMPOS");
        assertEsquemaError(respuesta);
    }

    @Test
    @DisplayName("CP-21: rechazar fechaHoraAtencion con tipo incorrecto")
    void cp21_fechaHoraConTipoIncorrecto() {
        String rut = ClienteReservas.crearPaciente(PerfilPaciente.particular());
        String cuerpo = """
                {"rutPaciente":"%s","especialidad":"MEDICINA_GENERAL",\
                "fechaHoraAtencion":12345,"canal":"WEB"}""".formatted(rut);

        Response respuesta = spec(AHORA).body(cuerpo).post("/api/reservas");

        assertError(respuesta, 400, "VAL-JSON");
        assertEsquemaError(respuesta);
    }

    @Test
    @DisplayName("CP-22: rechazar cuerpo JSON sintácticamente inválido")
    void cp22_jsonSintacticamenteInvalido() {
        String cuerpo = """
                {"rutPaciente":"11-1","especialidad":"MEDICINA_GENERAL",\
                "fechaHoraAtencion":"2026-10-06T10:00:00","canal":"WEB""";

        Response respuesta = spec(AHORA).body(cuerpo).post("/api/reservas");

        assertError(respuesta, 400, "VAL-JSON");
        assertEsquemaError(respuesta);
    }
}
