package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-07: tope de 3 reservas activas simultáneas por paciente (RN-RES-03).
// Expone DEF-03.
class TopeReservasActivasUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-07: la 4a reserva activa de un paciente se rechaza")
    @Disabled("DEF-03: la comparacion usa > en vez de >=, permite una 4a reserva activa. Ver Bitacora de Defectos.")
    void cp07_cuartaReservaActivaSeRechaza() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);

        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        for (String especialidad : new String[] {"MEDICINA_GENERAL", "NUTRICION", "PEDIATRIA"}) {
            new NuevaReservaPage(page, BASE_URL)
                    .navegar()
                    .crearReserva(rut, especialidad, bloqueNuevo(ahora), "WEB", false);
            assertThat(page.url()).doesNotContain("/reservas/nueva");
        }

        NuevaReservaPage cuartoIntento = new NuevaReservaPage(page, BASE_URL).navegar();
        cuartoIntento.crearReserva(rut, "CARDIOLOGIA", bloqueNuevo(ahora), "WEB", false);

        assertThat(page.url()).contains("/reservas/nueva");
        assertThat(cuartoIntento.mensajeError()).contains("RES-03-TOPE");
    }
}
