package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-13 y CP-14: anular desde estados que no lo permiten (RN-RES-06, RN-RES-07, RN-RES-10).
// Expone S-02 (CP-13) y DEF-07 / S-01 (CP-14); ambos fallan con el código actual (evidencia).
class AnulacionEstadosApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    // Falla con el código actual: S-02: el sistema permite anular una atencion EN_CURSO (200) en vez de rechazar con 409 RES-10-TRANSICION.
    @Test
    @DisplayName("CP-13: anular una atención EN_CURSO se rechaza")
    void cp13_anularEnCursoSeRechaza() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("EN_CURSO", reserva.id(), AHORA);
        LocalDateTime reloj = reserva.fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertError(respuesta, 409, "RES-10-TRANSICION");
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("EN_CURSO");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
    }

    // Falla con el código actual: DEF-07 / S-01: el sistema permite anular una reserva ATENDIDA (200) en vez de rechazar con 409 RES-10-TRANSICION.
    @Test
    @DisplayName("CP-14: anular una atención ATENDIDA se rechaza")
    void cp14_anularAtendidaSeRechaza() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("ATENDIDA", reserva.id(), AHORA);
        LocalDateTime reloj = reserva.fechaHora().plusHours(1);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertError(respuesta, 409, "RES-10-TRANSICION");
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ATENDIDA");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
    }
}
