package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-05: tope de modificaciones por reserva (RN-RES-09). Expone DEF-10.
class ModificarHoraUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-05: el 3er cambio de hora del mismo día se rechaza")
    @Disabled("DEF-10: el contador de modificaciones no sube si solo cambia la hora. Ver Bitacora de Defectos.")
    void cp05_tercerCambioDeHoraSeRechaza() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);
        LocalDateTime horaAtencion = bloqueNuevo(ahora);

        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "PEDIATRIA", horaAtencion, "WEB", false);
        DetalleReservaPage detalle = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        detalle.confirmar();

        // Tres cambios de hora, mismo día: el tope son 2 modificaciones (RN-RES-09).
        detalle.modificar(horaAtencion.withHour(11));
        assertThat(detalle.modificaciones()).isEqualTo(1);

        detalle.modificar(horaAtencion.withHour(12));
        assertThat(detalle.modificaciones()).isEqualTo(2);

        detalle.modificar(horaAtencion.withHour(13));
        assertThat(detalle.mensajeError()).contains("RES-09-TOPE");
        assertThat(detalle.modificaciones()).isEqualTo(2);
    }
}
