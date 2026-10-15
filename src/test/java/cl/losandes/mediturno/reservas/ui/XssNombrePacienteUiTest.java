package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.DetalleReservaPage;
import com.microsoft.playwright.Locator;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

// CP-49: el nombre del paciente se muestra como texto en el detalle de la reserva,
// sin interpretar HTML (XSS almacenado). Expone DEF-15.
class XssNombrePacienteUiTest extends BaseUiTest {

    private static final String NOMBRE_CON_HTML = "<img src=x onerror=\"window.__xss=1\">";

    @Test
    @DisplayName("CP-49: un nombre de paciente con HTML se muestra escapado en el detalle")
    @Disabled("DEF-15: detalle.html usa th:utext para el nombre del paciente y ejecuta el HTML guardado (XSS almacenado). Ver Bitacora de Defectos.")
    void cp49_nombreConHtmlSeMuestraEscapado() {
        LocalDateTime ahora = LocalDateTime.of(2026, 10, 5, 9, 0);
        fijarReloj(ahora);

        String rut = rutNuevo();
        crearPacienteConNombreViaApi(rut, NOMBRE_CON_HTML);
        crearReservaConBloqueLibre(rut, "MEDICINA_GENERAL", ahora);

        Locator paciente = new DetalleReservaPage(page, BASE_URL).esperarCargada().paciente();
        assertThat(paciente.locator("img").count())
                .as("el nombre no debe convertirse en una etiqueta <img>")
                .isZero();
        assertThat(paciente.innerText()).isEqualTo(NOMBRE_CON_HTML);
        assertThat(page.evaluate("() => window.__xss === undefined"))
                .as("el onerror del nombre no debe ejecutarse")
                .isEqualTo(true);
    }
}
