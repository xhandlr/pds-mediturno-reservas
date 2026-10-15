package cl.losandes.mediturno.reservas.ui;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.CheckedNode;
import com.deque.html.axecore.results.Rule;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
                String id = crearReservaDePrueba(playwright, baseUrl);
                for (String ruta : List.of("/", "/reservas/nueva", "/pacientes", "/reservas/" + id)) {
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

    /** Crea un paciente y una reserva por API y devuelve el id. Reintenta si el bloque está ocupado. */
    private String crearReservaDePrueba(Playwright playwright, String baseUrl) {
        APIRequestContext api = playwright.request().newContext(
                new APIRequest.NewContextOptions().setBaseURL(baseUrl));
        int n = ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999);
        String rut = n + "-" + (n % 10);
        api.post("/api/pacientes", RequestOptions.create()
                .setHeader("Content-Type", "application/json")
                .setData("""
                    {"rut":"%s","nombre":"Auditoria axe","fechaNacimiento":"1990-05-10",
                    "prevision":"PARTICULAR","tramoFonasa":null,"convenioEmpresa":false}
                    """.formatted(rut)));
        for (int intento = 0; intento < 10; intento++) {
            LocalDate dia = LocalDate.of(2026, 10, 5).plusDays(ThreadLocalRandom.current().nextInt(1, 56));
            if (dia.getDayOfWeek().getValue() >= 6) {
                continue;
            }
            APIResponse r = api.post("/api/reservas", RequestOptions.create()
                    .setHeader("Content-Type", "application/json")
                    .setHeader("X-Reloj-Simulado", "2026-10-05T09:00:00")
                    .setData("""
                        {"rutPaciente":"%s","especialidad":"MEDICINA_GENERAL",
                        "fechaHoraAtencion":"%sT10:00:00","canal":"WEB","aceptaListaEspera":false}
                        """.formatted(rut, dia)));
            if (r.status() == 201) {
                Matcher m = Pattern.compile("\"id\"\\s*:\\s*(\\d+)").matcher(r.text());
                if (m.find()) {
                    return m.group(1);
                }
            }
        }
        throw new AssertionError("No se pudo crear una reserva de prueba para el audit");
    }

}
