package cl.losandes.mediturno.reservas.ui;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Auditoría reproducible de los dos formularios críticos con WCAG 2.1 AA. */
class AccesibilidadAuditIT {

    @Test
    void auditarFormulariosCriticos() {
        String baseUrl = System.getProperty("ui.baseUrl", "http://localhost:8082");
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(true));
            try {
                Page page = browser.newPage();
                for (String ruta : List.of("/reservas/nueva", "/pacientes")) {
                    page.navigate(baseUrl + ruta);
                    AxeResults resultado = new AxeBuilder(page)
                            .withTags(List.of("wcag2a", "wcag2aa", "wcag21a", "wcag21aa"))
                            .analyze();
                    assertThat(resultado.isErrored()).as("axe-core en " + ruta).isFalse();
                    System.out.printf("A11Y|%s|violaciones=%d%n", ruta,
                            resultado.getViolations().size());
                    for (Rule regla : resultado.getViolations()) {
                        for (CheckedNode nodo : regla.getNodes()) {
                            System.out.printf("A11Y|%s|%s|%s|%s|%s|%s%n", ruta,
                                    regla.getId(), regla.getImpact(), regla.getTags(),
                                    nodo.getTarget(), nodo.getFailureSummary()
                                            .replace('\n', ' '));
                        }
                    }
                }
            } finally {
                browser.close();
            }
        }
    }
}
