package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
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

    // T02 BORRADOR -> EN_ESPERA; oráculo S-04: en bloque ocupado con aceptaListaEspera=true
    // la segunda queda EN_ESPERA (201) y la primera conserva su estado.
    @Test
    @DisplayName("CP-26: crear en bloque ocupado aceptando lista de espera deja la reserva EN_ESPERA")
    void cp26_crearEnBloqueOcupadoQuedaEnEspera() {
        Escenario e = armarEscenario();

        Response segunda = ClienteReservas.obtener(e.idSegunda(), AHORA);
        assertThat(segunda.jsonPath().getString("estado")).isEqualTo("EN_ESPERA");
        Response primera = ClienteReservas.obtener(e.primera().id(), AHORA);
        assertThat(primera.jsonPath().getString("estado")).isEqualTo("BORRADOR");
    }

    // T07 EN_ESPERA -> CONFIRMADA; oráculo S-07: al anular la reserva que ocupaba el cupo,
    // la EN_ESPERA más antigua del mismo bloque y especialidad pasa a CONFIRMADA.
    @Test
    @DisplayName("CP-27: anular la primera reserva promueve a CONFIRMADA la que está EN_ESPERA")
    void cp27_anularPrimeraPromueveEnEspera() {
        Escenario e = armarEscenario();
        LocalDateTime reloj = e.primera().fechaHora().minusMinutes(300);

        Response respuesta = ClienteReservas.anular(e.primera().id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP").isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        assertThat(ClienteReservas.obtener(e.primera().id(), reloj).jsonPath().getString("estado"))
                .isEqualTo("ANULADA");
        assertThat(ClienteReservas.obtener(e.idSegunda(), reloj).jsonPath().getString("estado"))
                .isEqualTo("CONFIRMADA");
    }

    // Falla estable: observa 200 frente a 409 esperado; clasificación normativa pendiente.
    @Test
    @Disabled("DEF-11: confirmar una reserva EN_ESPERA sin cupo responde 200 en vez de rechazarse con RES-CUPO. Ver Bitacora de Defectos.")
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
    @Disabled("DEF-12: anular una reserva EN_ESPERA cobra multa por anticipacion (multa 7500 en vez de 0). Ver Bitacora de Defectos.")
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
