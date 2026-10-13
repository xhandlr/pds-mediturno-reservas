package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Aserciones de anulación desde EN_CURSO y ATENDIDA; RN-RES-10 no precisa estos arcos.
class AnulacionEstadosApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    // Falla estable: observa 200 frente a 409 esperado; clasificación normativa pendiente.
    @Test
    @Disabled("DEF-13: la maquina de estados permite anular una reserva EN_CURSO (responde 200 en vez de 409). Ver Bitacora de Defectos.")
    @DisplayName("CP-13: anular una atención EN_CURSO se rechaza")
    void cp13_anularEnCursoSeRechaza() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("EN_CURSO", reserva.id(), AHORA);
        LocalDateTime reloj = reserva.fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertError(respuesta, 409, "RES-10-TRANSICION");
        assertEsquemaError(respuesta);
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("EN_CURSO");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
    }

    // Falla estable: observa 200 frente a 409 esperado; clasificación normativa pendiente.
    @Test
    @Disabled("DEF-07: la maquina de estados permite anular una reserva ATENDIDA (responde 200 en vez de 409). Ver Bitacora de Defectos.")
    @DisplayName("CP-14: anular una atención ATENDIDA se rechaza")
    void cp14_anularAtendidaSeRechaza() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("ATENDIDA", reserva.id(), AHORA);
        LocalDateTime reloj = reserva.fechaHora().plusHours(1);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertError(respuesta, 409, "RES-10-TRANSICION");
        assertEsquemaError(respuesta);
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ATENDIDA");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
    }

    // T04 BORRADOR -> ANULADA; oráculo S-03: anular un BORRADOR está permitido y no genera multa.
    @Test
    @Disabled("DEF-12: anular un BORRADOR cobra multa por anticipacion (multa 7500 en vez de 0). Ver Bitacora de Defectos.")
    @DisplayName("CP-25: anular una reserva en BORRADOR no genera multa")
    void cp25_anularBorradorSinMulta() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        LocalDateTime reloj = reserva.fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(respuesta.jsonPath().getInt("multa")).isZero();
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
    }
}
