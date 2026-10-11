package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RelojSimuladoApiTest extends BaseApiTest {

    @Test
    @DisplayName("RN-RES-02: una reserva anterior al reloj simulado se rechaza")
    void reservaAnteriorAlRelojSimuladoSeRechaza() {

        LocalDateTime relojSimulado =
                LocalDateTime.of(2026, 10, 5, 9, 0);

        LocalDateTime fechaAtencion =
                LocalDateTime.of(2026, 10, 5, 8, 40);

        String rut = ClienteReservas.crearPaciente(
                PerfilPaciente.particular()
        );

        Response respuesta = ClienteReservas.crearReserva(
                rut,
                "MEDICINA_GENERAL",
                fechaAtencion,
                "WEB",
                false,
                relojSimulado
        );

        assertError(
                respuesta,
                400,
                "RES-02-PASADO"
        );

        assertEsquemaError(respuesta);

        assertThat(fechaAtencion)
                .isBefore(relojSimulado);
    }
}
