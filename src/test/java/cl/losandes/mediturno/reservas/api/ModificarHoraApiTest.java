package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-09 y CP-12: modificar la hora de una reserva en BORRADOR y CONFIRMADA (RN-RES-09, RN-RES-10).
class ModificarHoraApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    /** Bloque nuevo en un día distinto al dado (lunes a viernes, lo garantiza el asignador). */
    private static LocalDateTime bloqueEnOtroDia(LocalDateTime original) {
        while (true) {
            LocalDateTime bloque = AsignadorBloques.siguiente(AHORA);
            if (!bloque.toLocalDate().equals(original.toLocalDate())) {
                return bloque;
            }
        }
    }

    @Test
    @DisplayName("CP-09: modificar la hora de una reserva en BORRADOR a otro día")
    void cp09_modificarBorradorAOtroDia() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        assertThat(reserva.estado()).isEqualTo("BORRADOR");
        LocalDateTime nueva = bloqueEnOtroDia(reserva.fechaHora());

        Response respuesta = ClienteReservas.modificar(reserva.id(), nueva, AHORA);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("BORRADOR");
        assertThat(respuesta.jsonPath().getInt("modificaciones")).isEqualTo(1);
        assertThat(LocalDateTime.parse(respuesta.jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(nueva);
        assertThat(respuesta.jsonPath().getInt("copago")).isGreaterThanOrEqualTo(0);

        Response consulta = ClienteReservas.obtener(reserva.id(), AHORA);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("BORRADOR");
        assertThat(consulta.jsonPath().getInt("modificaciones")).isEqualTo(1);
        assertThat(LocalDateTime.parse(consulta.jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(nueva);
    }

    @Test
    @DisplayName("CP-12: modificar la hora de una reserva CONFIRMADA a otro día")
    void cp12_modificarConfirmadaAOtroDia() {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);
        LocalDateTime nueva = bloqueEnOtroDia(reserva.fechaHora());

        Response respuesta = ClienteReservas.modificar(reserva.id(), nueva, AHORA);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("CONFIRMADA");
        assertThat(respuesta.jsonPath().getInt("modificaciones")).isEqualTo(1);
        assertThat(LocalDateTime.parse(respuesta.jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(nueva);
        assertThat(respuesta.jsonPath().getInt("copago")).isGreaterThanOrEqualTo(0);

        Response consulta = ClienteReservas.obtener(reserva.id(), AHORA);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("CONFIRMADA");
        assertThat(consulta.jsonPath().getInt("modificaciones")).isEqualTo(1);
    }
}
