package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** CP-02: recorre el camino principal de la máquina de estados, RN-RES-10. */
class CicloVidaReservaUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-02: confirmar, iniciar y finalizar deja la reserva en ATENDIDA")
    void cp02_cicloCompletoTerminaEnAtendida() {
        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);
        LocalDateTime horaAtencion = bloqueNuevo(ahora);

        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "NUTRICION", horaAtencion, "WEB", false);

        DetalleReservaPage detalle = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        assertThat(detalle.estado()).isEqualTo("BORRADOR");

        detalle.confirmar();
        assertThat(detalle.estado()).isEqualTo("CONFIRMADA");

        detalle.iniciar();
        assertThat(detalle.estado()).isEqualTo("EN_CURSO");

        detalle.finalizar();
        assertThat(detalle.estado()).isEqualTo("ATENDIDA");
    }
}
