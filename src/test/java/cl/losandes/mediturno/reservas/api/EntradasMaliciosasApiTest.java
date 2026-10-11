package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EntradasMaliciosasApiTest extends BaseApiTest {

    @Test
    @DisplayName("CP-23: tratar intento de inyección como un dato inválido")
    void cp23_intentoInyeccionNoAlteraRegistros() {
        ReservaCreada existente = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), "MEDICINA_GENERAL", AHORA);
        String cuerpo = """
                {"rutPaciente":"%s","especialidad":"' OR '1'='1",\
                "fechaHoraAtencion":"2026-10-06T10:00:00","canal":"WEB"}"""
                .formatted(existente.rut());

        Response respuesta = spec(AHORA).body(cuerpo).post("/api/reservas");

        assertError(respuesta, 400, "VAL-ENUM");
        assertEsquemaError(respuesta);

        Response consulta = ClienteReservas.obtener(existente.id(), AHORA);
        assertThat(consulta.statusCode()).isEqualTo(200);
        assertEsquema(consulta, "reserva.schema.json");
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("BORRADOR");
        assertThat(consulta.jsonPath().getString("especialidad"))
                .isEqualTo("MEDICINA_GENERAL");
    }

    @Test
    @DisplayName("CP-24: rechazar nombre de paciente superior al máximo contractual")
    void cp24_nombrePacienteSobredimensionado() {
        String rut = ClienteReservas.rutNuevo();
        String nombre = "X".repeat(1_000);
        String cuerpo = """
                {"rut":"%s","nombre":"%s","fechaNacimiento":"1986-05-10",\
                "prevision":"PARTICULAR","tramoFonasa":null,"convenioEmpresa":false}"""
                .formatted(rut, nombre);

        Response respuesta = spec(AHORA).body(cuerpo).post("/api/pacientes");

        assertError(respuesta, 400, "VAL-CAMPOS");
        assertEsquemaError(respuesta);
    }
}
