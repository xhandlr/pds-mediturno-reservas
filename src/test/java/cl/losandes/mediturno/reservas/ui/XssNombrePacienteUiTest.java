package cl.losandes.mediturno.reservas.ui;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-49: el nombre del paciente se muestra como texto en el detalle de la reserva,
// sin interpretar HTML (XSS almacenado). Expone DEF-15.
class XssNombrePacienteUiTest extends BaseUiTest {

    private static final String NOMBRE_MALICIOSO = "<img src=x onerror=\"window.__xss=1\">";

    @Test
    @DisplayName("CP-49: un nombre de paciente con HTML se muestra escapado en el detalle")
    @Disabled("DEF-15: detalle.html usa th:utext para el nombre del paciente y ejecuta el HTML guardado (XSS almacenado). Ver Bitacora de Defectos.")
    void cp49_nombreConHtmlSeMuestraEscapado() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);

        String rut = rutNuevo();
        crearPacienteConNombre(rut, NOMBRE_MALICIOSO);
        crearReservaConBloqueLibre(rut, "MEDICINA_GENERAL", ahora);

        Locator paciente = page.locator("[data-testid=detalle-paciente]");
        assertThat(paciente.locator("img").count())
                .as("el nombre no debe convertirse en una etiqueta <img>")
                .isZero();
        assertThat(paciente.innerText()).isEqualTo(NOMBRE_MALICIOSO);
        assertThat(page.evaluate("() => window.__xss === undefined"))
                .as("el onerror del nombre no debe ejecutarse")
                .isEqualTo(true);
    }

    private void crearPacienteConNombre(String rut, String nombre) {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext api = playwright.request().newContext(
                    new APIRequest.NewContextOptions().setBaseURL(BASE_URL));
            APIResponse respuesta = api.post("/api/pacientes", RequestOptions.create()
                    .setHeader("Content-Type", "application/json")
                    .setData("""
                    {
                      "rut": "%s",
                      "nombre": "%s",
                      "fechaNacimiento": "1990-05-10",
                      "prevision": "PARTICULAR",
                      "tramoFonasa": null,
                      "convenioEmpresa": false
                    }
                    """.formatted(rut, nombre.replace("\"", "\\\""))));
            assertThat(respuesta.status())
                    .as("la API acepta el nombre con HTML (no lo filtra)")
                    .isEqualTo(201);
        }
    }
}
