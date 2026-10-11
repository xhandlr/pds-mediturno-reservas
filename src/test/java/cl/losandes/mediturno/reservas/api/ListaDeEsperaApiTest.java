package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// Aserciones sobre salida de la lista de espera; RN-RES-10 no precisa estos arcos.
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
        assertEsquema(segunda, "reserva.schema.json");
        assertThat(segunda.jsonPath().getString("estado")).isEqualTo("EN_ESPERA");
        return new Escenario(primera, segunda.jsonPath().getLong("id"));
    }

    // Falla estable: observa 200 frente a 409 esperado; clasificación normativa pendiente.
    @Test
    @DisplayName("CP-10: confirmar desde la lista de espera sin cupo se rechaza")
    void cp10_confirmarEnEsperaSinCupo() {
        Escenario e = armarEscenario();
        String estadoPrimera = ClienteReservas.obtener(e.primera().id(), AHORA)
                .jsonPath().getString("estado");

        Response respuesta = ClienteReservas.confirmar(e.idSegunda(), AHORA);

        assertError(respuesta, 409, "RES-CUPO");
        assertEsquemaError(respuesta);
        assertThat(ClienteReservas.obtener(e.idSegunda(), AHORA).jsonPath().getString("estado"))
                .isEqualTo("EN_ESPERA");
        assertThat(ClienteReservas.obtener(e.primera().id(), AHORA).jsonPath().getString("estado"))
                .isEqualTo(estadoPrimera);
    }

    // Falla estable: observa multa 7500 frente a 0 esperado; RN no precisa multa de EN_ESPERA.
    @Test
    @DisplayName("CP-11: anular una reserva EN_ESPERA no genera multa")
    void cp11_anularEnEsperaSinMulta() {
        Escenario e = armarEscenario();
        String estadoPrimera = ClienteReservas.obtener(e.primera().id(), AHORA)
                .jsonPath().getString("estado");
        LocalDateTime reloj = e.primera().fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(e.idSegunda(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(respuesta.jsonPath().getInt("multa")).isZero();
        Response consulta = ClienteReservas.obtener(e.idSegunda(), reloj);
        assertThat(consulta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        assertThat(consulta.jsonPath().getInt("multa")).isZero();
        assertThat(ClienteReservas.obtener(e.primera().id(), reloj).jsonPath().getString("estado"))
                .isEqualTo(estadoPrimera);
    }
}
