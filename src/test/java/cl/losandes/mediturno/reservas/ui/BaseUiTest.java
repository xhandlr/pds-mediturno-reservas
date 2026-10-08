package cl.losandes.mediturno.reservas.ui;

import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Arranca un navegador nuevo por cada método de prueba.
 *
 * Se exige ejecución paralela y cero dependencias de orden. En este caso,
 * Playwright no es seguro entre hilos si se comparte una sola instancia, así que cada test levanta y cierra su propio Playwright + Browser + BrowserContext, sin compartir nada con los demás.
 */
public abstract class BaseUiTest {

    protected static final String BASE_URL =
            System.getProperty("ui.baseUrl", "http://localhost:8082");

    private Playwright playwright;
    private Browser browser;
    private BrowserContext contexto;
    protected Page page;

    @BeforeEach
    void arrancarNavegador() {
        playwright = Playwright.create();
        boolean headless = !"false".equals(System.getProperty("ui.headless", "true"));
        browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                .setHeadless(headless)
                .setSlowMo(headless ? 0 : 300));
        contexto = browser.newContext();
        page = contexto.newPage();
    }

    /**
     * Fija la hora simulada (cabecera X-Reloj-Simulado) para todas las peticiones de este
     * test. Sin esto el test depende del reloj real y es inestable por construcción.
     */
    protected void fijarReloj(LocalDateTime instante) {
        contexto.setExtraHTTPHeaders(Map.of(
                "X-Reloj-Simulado", instante.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
    }

    @AfterEach
    void cerrarNavegador() {
        contexto.close();
        browser.close();
        playwright.close();
    }

    // ------------------------------------------------------- datos de prueba

    /** RUT único por ejecución, para que cada escenario tenga su propio paciente. */
    protected String rutNuevo() {
        int n = ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999);
        return n + "-" + (n % 10);
    }

    /**
     * Día y hora únicos por ejecución: la base es persistente, así que repetir siempre el
     * mismo bloque choca contra lo que haya dejado una corrida anterior, o contra otro test
     * corriendo en paralelo (RES-CUPO). Día hábil entre 1 y 55 días después del reloj dado
     * (dentro de la ventana de 60 días, con margen), saltando sábado y domingo, hora entre
     * 09 y 16 en punto.
     */
    protected LocalDateTime bloqueNuevo(LocalDateTime ahora) {
        java.time.LocalDate dia;
        do {
            int diasAdelante = ThreadLocalRandom.current().nextInt(1, 56);
            dia = ahora.toLocalDate().plusDays(diasAdelante);
        } while (dia.getDayOfWeek() == java.time.DayOfWeek.SATURDAY
                || dia.getDayOfWeek() == java.time.DayOfWeek.SUNDAY);
        int hora = ThreadLocalRandom.current().nextInt(9, 17);
        return dia.atTime(hora, 0);
    }

    protected void crearPacienteViaApi(String rut) {
        crearPacienteViaApi(rut, "PARTICULAR", null);
    }

    /**
     * Paciente FONASA con el tramo dado: a diferencia de PARTICULAR, acá la bonificación
     * deja el copago por debajo del arancel bruto, lo que hace falta para notar defectos
     * que confunden una base de cálculo con otra (ej. DEF-06).
     */
    protected void crearPacienteFonasaViaApi(String rut, String tramo) {
        crearPacienteViaApi(rut, "FONASA", tramo);
    }

    private void crearPacienteViaApi(String rut, String prevision, String tramoFonasa) {
        try (Playwright playwright = Playwright.create()) {
            APIRequestContext api = playwright.request().newContext(
                    new APIRequest.NewContextOptions().setBaseURL(BASE_URL));
            String tramoJson = tramoFonasa == null ? "null" : "\"" + tramoFonasa + "\"";
            APIResponse respuesta = api.post("/api/pacientes", RequestOptions.create()
                    .setHeader("Content-Type", "application/json")
                    .setData(
                    """
                    {
                      "rut": "%s",
                      "nombre": "Paciente de Prueba UI",
                      "fechaNacimiento": "1990-05-10",
                      "prevision": "%s",
                      "tramoFonasa": %s,
                      "convenioEmpresa": false
                    }
                    """.formatted(rut, prevision, tramoJson)));
            assertThat(respuesta.status()).as("creacion del paciente de prueba").isEqualTo(201);
        }
    }
}
