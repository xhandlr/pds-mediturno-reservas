package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-08: bloqueo por 3 inasistencias en 6 meses móviles (RN-RES-08). Expone DEF-05.
class BloqueoPorInasistenciasUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-08: 3 inasistencias cruzando el año bloquean reservar por WEB")
    @Disabled("DEF-05: la ventana de 6 meses parte el 1 de enero en vez de ser movil. Ver Bitacora de Defectos.")
    void cp08_tresInasistenciasBloqueanReservaPorWeb() {
        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        registrarInasistencia(rut, LocalDateTime.of(2025, 11, 20, 10, 0),
                LocalDateTime.of(2025, 11, 15, 9, 0));
        registrarInasistencia(rut, LocalDateTime.of(2025, 12, 10, 10, 0),
                LocalDateTime.of(2025, 12, 5, 9, 0));
        registrarInasistencia(rut, LocalDateTime.of(2026, 1, 5, 10, 0),
                LocalDateTime.of(2025, 12, 31, 9, 0));

        // Las tres caen dentro de los 6 meses previos al 10 de enero de 2026.
        fijarReloj(LocalDateTime.of(2026, 1, 10, 9, 0));
        NuevaReservaPage intento = new NuevaReservaPage(page, BASE_URL).navegar();
        intento.crearReserva(rut, "NUTRICION", LocalDateTime.of(2026, 1, 12, 10, 0), "WEB", false);

        assertThat(page.url()).contains("/reservas/nueva");
        assertThat(intento.mensajeError()).contains("RES-08-BLOQUEO");
    }

    private void registrarInasistencia(String rut, LocalDateTime fechaAtencion, LocalDateTime ahoraCreacion) {
        fijarReloj(ahoraCreacion);
        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "MEDICINA_GENERAL", fechaAtencion, "WEB", false);
        DetalleReservaPage detalle = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        detalle.confirmar();
        detalle.marcarInasistencia();
    }
}
