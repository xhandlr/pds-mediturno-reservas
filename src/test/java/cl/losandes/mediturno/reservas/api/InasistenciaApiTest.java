package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Aserciones de inasistencia desde CONFIRMADA; RN-RES-07 fija la multa, RN-RES-10 el estado.
class InasistenciaApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    // T11 CONFIRMADA -> NO_ASISTIDA; oráculos S-10 (multa = 50 % del copago, redondeo al peso
    // superior, RN-RES-07) y S-11 (la inasistencia no se condiciona a la hora de la cita).
    @Test
    @DisplayName("CP-28: registrar inasistencia de una reserva CONFIRMADA cobra el 50 % del copago")
    void cp28_inasistenciaConfirmadaMulta50() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        Response confirmada = ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);
        int copago = confirmada.jsonPath().getInt("copago");
        LocalDateTime reloj = reserva.fechaHora().plusMinutes(30);

        Response respuesta = ClienteReservas.inasistencia(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        int multaEsperada = (int) Math.ceil(copago * 0.5);
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("NO_ASISTIDA");
        assertThat(respuesta.jsonPath().getInt("multa")).isEqualTo(multaEsperada);
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("NO_ASISTIDA");
        assertThat(consulta.jsonPath().getInt("multa")).isEqualTo(multaEsperada);
    }
}
