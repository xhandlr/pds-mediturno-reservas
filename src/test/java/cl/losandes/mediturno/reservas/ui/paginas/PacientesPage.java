package cl.losandes.mediturno.reservas.ui.paginas;

import com.microsoft.playwright.Page;

/** Página "/pacientes": padrón de pacientes. */
public class PacientesPage extends BasePage {

    public PacientesPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public PacientesPage navegar() {
        page.navigate(baseUrl + "/pacientes");
        testid("tabla-pacientes").waitFor();
        return this;
    }

    public boolean contieneRut(String rut) {
        return testid("tabla-pacientes").locator("text=" + rut).count() > 0;
    }
}
