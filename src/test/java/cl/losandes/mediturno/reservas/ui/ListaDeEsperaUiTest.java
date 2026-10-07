package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
* CP-03: lista de espera y su promoción al anular la reserva que ocupaba el cupo.
*/
class ListaDeEsperaUiTest extends BaseUiTest {

    @Test
    @DisplayName("CP-03: la 2a reserva en lista de espera se promueve al anular la 1a")
    void cp03_listaDeEsperaSePromueveAlAnularLaActiva() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);
        LocalDateTime bloque = bloqueNuevo(ahora);

        String rutUno = rutNuevo();
        crearPacienteViaApi(rutUno);
        String rutDos = rutNuevo();
        crearPacienteViaApi(rutDos);

        // 1a reserva: ocupa el único cupo del bloque.
        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rutUno, "CARDIOLOGIA", bloque, "WEB", false);
        DetalleReservaPage detalleUno = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        assertThat(detalleUno.estado()).isEqualTo("BORRADOR");
        long idUno = idDesdeUrl();

        // 2a reserva: mismo bloque y especialidad, acepta lista de espera.
        new NuevaReservaPage(page, BASE_URL)
                .navegar()
                .crearReserva(rutDos, "CARDIOLOGIA", bloque, "WEB", true);
        DetalleReservaPage detalleDos = new DetalleReservaPage(page, BASE_URL).esperarCargada();
        assertThat(detalleDos.estado()).isEqualTo("EN_ESPERA");
        long idDos = idDesdeUrl();

        // Al anular la 1a, la 2a (en espera) debe promoverse a CONFIRMADA.
        detalleUno.navegar(idUno).anular();
        detalleDos.navegar(idDos);
        assertThat(detalleDos.estado()).isEqualTo("CONFIRMADA");
    }

    /** Tomcat agrega ";jsessionid=..." a la URL cuando no hay cookies; hay que descartarlo. */
    private long idDesdeUrl() {
        String url = page.url();
        String ultimoSegmento = url.substring(url.lastIndexOf('/') + 1);
        int finDelId = ultimoSegmento.indexOf(';');
        if (finDelId >= 0) {
            ultimoSegmento = ultimoSegmento.substring(0, finDelId);
        }
        return Long.parseLong(ultimoSegmento);
    }
}
