package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Aserciones de anulación desde EN_CURSO y ATENDIDA; RN-RES-10 no precisa estos arcos.
class AnulacionEstadosApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    // Falla estable: observa 200 frente a 409 esperado; clasificación normativa pendiente.
    @Test
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
}
