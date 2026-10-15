package cl.losandes.mediturno.reservas.ui.paginas;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;

/** Comportamiento común a todas las páginas de la interfaz web. */
public abstract class BasePage {

    protected final Page page;
    protected final String baseUrl;

    protected BasePage(Page page, String baseUrl) {
        this.page = page;
        this.baseUrl = baseUrl;
    }

    protected Locator testid(String id) {
        return page.locator("[data-testid=" + id + "]");
    }

    public void irAListadoReservas() {
        testid("nav-reservas").click();
    }

    public void irANuevaReserva() {
        testid("nav-nueva").click();
    }

    public void irAPacientes() {
        testid("nav-pacientes").click();
    }

    /** Texto del mensaje de éxito, si la página lo muestra. Espera a que sea visible. */
    public String mensajeExito() {
        Locator mensaje = testid("mensaje-exito");
        mensaje.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return mensaje.innerText();
    }

    /** Texto del mensaje de error, si la página lo muestra. Espera a que sea visible. */
    public String mensajeError() {
        Locator mensaje = testid("mensaje-error");
        mensaje.waitFor(new Locator.WaitForOptions().setState(WaitForSelectorState.VISIBLE));
        return mensaje.innerText();
    }

    public boolean hayMensajeError() {
        return testid("mensaje-error").isVisible();
    }
}
