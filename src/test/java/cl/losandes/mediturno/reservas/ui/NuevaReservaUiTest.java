package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/** CP-01: crear una reserva desde el formulario web. */
class NuevaReservaUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-01: crear una reserva por UI deja la reserva en BORRADOR")
    void cp01_crearReservaQuedaEnBorrador() {
        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        // Hora simulada fija (lunes); el bloque de atención se sortea en cada corrida
        // para no chocar con reservas que haya dejado una ejecución anterior.
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);
        LocalDateTime horaAtencion = bloqueNuevo(ahora);

        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "MEDICINA_GENERAL", horaAtencion, "WEB", false);

        // Al crear con éxito, el controlador redirige al detalle de la reserva, no al listado.
        DetalleReservaPage detalle = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        assertThat(detalle.mensajeExito()).isNotBlank();
        assertThat(detalle.estado()).isEqualTo("BORRADOR");
    }
}
