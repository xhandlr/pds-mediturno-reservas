package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-04: multa por anulación según la anticipación (RN-RES-05, 06, 07). Expone DEF-06.
class MultaAnulacionUiTest extends BaseUiTest {

    @ParameterizedTest(name = "anulación a {0} min de anticipación -> multa {1}%")
    @Disabled("DEF-06: la multa se calcula sobre el arancel bruto, no sobre el copago. Ver Bitacora de Defectos.")
    @CsvSource({
            "300, 0",
            "120, 30",
            "30, 50"
    })
    void cp04_multaSegunAnticipacion(int minutosAntes, int porcentajeEsperado) {
        LocalDateTime ahoraCreacion = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahoraCreacion);
        LocalDateTime horaAtencion = bloqueNuevo(ahoraCreacion);

        String rut = rutNuevo();
        crearPacienteFonasaViaApi(rut, "B"); // copago = 10% del arancel bruto, bien distinto

        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rut, "PSIQUIATRIA", horaAtencion, "WEB", false);
        DetalleReservaPage detalle = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        long copago = detalle.copago();
        detalle.confirmar();

        fijarReloj(horaAtencion.minusMinutes(minutosAntes));
        detalle.anular();

        long multaEsperada = copago * porcentajeEsperado / 100;
        assertThat(detalle.multa()).isEqualTo(multaEsperada);
    }
}
