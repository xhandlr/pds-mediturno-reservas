package cl.losandes.mediturno.reservas.ui.paginas;

import com.microsoft.playwright.Page;

/** Página "/": listado y búsqueda de reservas. */
public class ListadoReservasPage extends BasePage {

    public ListadoReservasPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public ListadoReservasPage navegar() {
        page.navigate(baseUrl + "/");
        testid("tabla-reservas").waitFor();
        return this;
    }

    public ListadoReservasPage buscarPorRut(String rut) {
        testid("filtro-rut").fill(rut);
        testid("filtro-buscar").click();
        testid("tabla-reservas").waitFor();
        return this;
    }

    public boolean sinResultados() {
        return testid("sin-resultados").isVisible();
    }

    public String estadoDeFila(long idReserva) {
        return page.locator("[data-testid=fila-reserva-" + idReserva + "] [data-testid=celda-estado]").innerText();
    }

    public DetalleReservaPage verDetalle(long idReserva) {
        testid("ver-" + idReserva).click();
        return new DetalleReservaPage(page, baseUrl).esperarCargada();
    }
}
