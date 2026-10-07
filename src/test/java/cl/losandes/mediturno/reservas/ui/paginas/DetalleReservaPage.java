package cl.losandes.mediturno.reservas.ui.paginas;

import com.microsoft.playwright.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Página "/reservas/{id}": detalle y transiciones de una reserva. */
public class DetalleReservaPage extends BasePage {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public DetalleReservaPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public DetalleReservaPage navegar(long idReserva) {
        page.navigate(baseUrl + "/reservas/" + idReserva);
        return esperarCargada();
    }

    public DetalleReservaPage esperarCargada() {
        testid("detalle-id").waitFor();
        return this;
    }

    public String estado() {
        return testid("detalle-estado").innerText();
    }

    public long copago() {
        return Long.parseLong(testid("detalle-copago").innerText().trim());
    }

    public long multa() {
        return Long.parseLong(testid("detalle-multa").innerText().trim());
    }

    public int modificaciones() {
        return Integer.parseInt(testid("detalle-modificaciones").innerText().trim());
    }

    public DetalleReservaPage confirmar() {
        testid("btn-confirmar").click();
        return esperarCargada();
    }

    public DetalleReservaPage iniciar() {
        testid("btn-iniciar").click();
        return esperarCargada();
    }

    public DetalleReservaPage finalizar() {
        testid("btn-finalizar").click();
        return esperarCargada();
    }

    public DetalleReservaPage anular() {
        testid("btn-anular").click();
        return esperarCargada();
    }

    public DetalleReservaPage marcarInasistencia() {
        testid("btn-inasistencia").click();
        return esperarCargada();
    }

    public DetalleReservaPage modificar(LocalDateTime nuevaFechaHora) {
        testid("input-nueva-fecha").fill(nuevaFechaHora.format(FORMATO_FECHA));
        testid("btn-modificar").click();
        return esperarCargada();
    }
}
