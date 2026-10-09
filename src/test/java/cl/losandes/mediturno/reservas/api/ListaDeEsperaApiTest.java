package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-10 y CP-11: salida de la lista de espera (RN-RES-05, RN-RES-10).
// Expone S-05 (CP-10) y S-06 (CP-11); ambos fallan con el código actual (evidencia).
class ListaDeEsperaApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    /** Primera reserva (BORRADOR) y segunda en el mismo bloque, que queda EN_ESPERA. */
    private record Escenario(ReservaCreada primera, long idSegunda) { }

    private static Escenario armarEscenario() {
        ReservaCreada primera = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        String rutSegundo = ClienteReservas.crearPaciente(PerfilPaciente.particular());
        Response segunda = ClienteReservas.crearReserva(rutSegundo, ESPECIALIDAD,
                primera.fechaHora(), "WEB", true, AHORA);
        assertThat(segunda.statusCode()).as("creacion 2a reserva: " + segunda.asString()).isEqualTo(201);
        assertThat(segunda.jsonPath().getString("estado")).isEqualTo("EN_ESPERA");
        return new Escenario(primera, segunda.jsonPath().getLong("id"));
    }

    // Falla con el código actual: S-05 / C-03: el sistema confirma desde EN_ESPERA sin validar cupo y responde 200 en vez de 409 RES-CUPO.
    @Test
    @DisplayName("CP-10: confirmar desde la lista de espera sin cupo se rechaza")
    void cp10_confirmarEnEsperaSinCupo() {
        Escenario e = armarEscenario();
        String estadoPrimera = ClienteReservas.obtener(e.primera().id(), AHORA)
                .jsonPath().getString("estado");

        Response respuesta = ClienteReservas.confirmar(e.idSegunda(), AHORA);

        assertError(respuesta, 409, "RES-CUPO");
        assertThat(ClienteReservas.obtener(e.idSegunda(), AHORA).jsonPath().getString("estado"))
                .isEqualTo("EN_ESPERA");
        assertThat(ClienteReservas.obtener(e.primera().id(), AHORA).jsonPath().getString("estado"))
                .isEqualTo(estadoPrimera);
    }

    // Falla con el código actual: S-06: el sistema cobra multa (30 %, 7500 en este caso) al anular una reserva EN_ESPERA; el esperado es multa 0. No es DEF-06: el paciente es PARTICULAR y el copago coincide con el arancel.
    @Test
    @DisplayName("CP-11: anular una reserva EN_ESPERA no genera multa")
    void cp11_anularEnEsperaSinMulta() {
        Escenario e = armarEscenario();
        String estadoPrimera = ClienteReservas.obtener(e.primera().id(), AHORA)
                .jsonPath().getString("estado");
        LocalDateTime reloj = e.primera().fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(e.idSegunda(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(respuesta.jsonPath().getInt("multa")).isZero();
        Response consulta = ClienteReservas.obtener(e.idSegunda(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
        assertThat(ClienteReservas.obtener(e.primera().id(), reloj).jsonPath().getString("estado"))
                .isEqualTo(estadoPrimera);
    }
}
