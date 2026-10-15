package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-06: reserva duplicada, misma especialidad y mismo día (RN-RES-04).
// Expone DEF-04.
class ReservaDuplicadaUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-06: 2a reserva misma especialidad y día, distinta hora, se rechaza")
    @Disabled("DEF-04: la validacion compara el instante exacto, no solo el dia. Ver Bitacora de Defectos.")
    void cp06_segundaReservaMismoDiaSeRechaza() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);
        LocalDateTime primeraHora = bloqueNuevo(ahora);
        LocalDateTime segundaHora = primeraHora.getHour() < 15
                ? primeraHora.plusHours(2)
                : primeraHora.minusHours(2);

        String rut = rutNuevo();
        crearPacienteViaApi(rut);

        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "CARDIOLOGIA", primeraHora, "WEB", false);

        NuevaReservaPage segundoIntento = new NuevaReservaPage(page, BASE_URL).navegar();
        segundoIntento.crearReserva(rut, "CARDIOLOGIA", segundaHora, "WEB", false);

        assertThat(page.url()).contains("/reservas/nueva");
        assertThat(segundoIntento.mensajeError()).contains("RES-04-DUPLICADA");
    }
}
