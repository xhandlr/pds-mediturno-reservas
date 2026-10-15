package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Valores límite de la anticipación de anulación (RN-RES-05, RN-RES-06, RN-RES-07): 240, 239,
// 60 y 59 minutos. Lectura literal del texto: "más de 4 horas" excluye 240 (30 %);
// "entre 4 horas y 1 hora" incluye 240 y 60 (30 %); "menos de 1 hora" excluye 60 (50 %).
// Paciente PARTICULAR (copago == arancel, no enmascara DEF-06); la multa se calcula sobre el
// copago que devuelve la API, redondeada al peso superior.
class ValoresLimiteAnulacionApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    private static void anularConAnticipacion(int minutos, int porcentajeEsperado) {
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        Response confirmada = ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);
        int copago = confirmada.jsonPath().getInt("copago");
        LocalDateTime reloj = reserva.fechaHora().minusMinutes(minutos);

        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        int multaEsperada = (copago * porcentajeEsperado + 99) / 100;
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(respuesta.jsonPath().getInt("multa")).isEqualTo(multaEsperada);
        Response consulta = ClienteReservas.obtener(reserva.id(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(consulta.jsonPath().getInt("multa")).isEqualTo(multaEsperada);
    }

    @Test
    @DisplayName("CP-45: anular CONFIRMADA a 240 min exactos cobra el 30 % del copago")
    void cp45_anular240MinutosMulta30() {
        anularConAnticipacion(240, 30);
    }

    @Test
    @DisplayName("CP-46: anular CONFIRMADA a 239 min cobra el 30 % del copago")
    void cp46_anular239MinutosMulta30() {
        anularConAnticipacion(239, 30);
    }

    @Test
    @DisplayName("CP-47: anular CONFIRMADA a 60 min exactos cobra el 30 % del copago")
    void cp47_anular60MinutosMulta30() {
        anularConAnticipacion(60, 30);
    }

    @Test
    @DisplayName("CP-48: anular CONFIRMADA a 59 min cobra el 50 % del copago")
    void cp48_anular59MinutosMulta50() {
        anularConAnticipacion(59, 50);
    }
}
