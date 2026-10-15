package cl.losandes.mediturno.reservas.ui;

import cl.losandes.mediturno.reservas.ui.paginas.NuevaReservaPage;
import com.microsoft.playwright.APIRequest;
import com.microsoft.playwright.APIRequestContext;
import com.microsoft.playwright.APIResponse;
import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.RequestOptions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.net.URI;
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

    /** Crea por UI y reintenta solo si la base persistente ya ocupa el bloque sorteado. */
    protected LocalDateTime crearReservaConBloqueLibre(String rut, String especialidad,
                                                       LocalDateTime ahora) {
        for (int intento = 0; intento < 10; intento++) {
            LocalDateTime bloque = bloqueNuevo(ahora);
            new NuevaReservaPage(page, BASE_URL)
                    .navegar()
                    .crearReserva(rut, especialidad, bloque, "WEB", false);
            page.waitForLoadState(LoadState.DOMCONTENTLOADED);
            String ruta = URI.create(page.url()).getPath().split(";", 2)[0];
            if (ruta.matches("/reservas/\\d+")) {
                return bloque;
            }
            assertThat(ruta).as("redirección tras crear la reserva").isEqualTo("/reservas/nueva");
            assertThat(page.locator("[data-testid=mensaje-error]").textContent())
                    .as("solo se reintenta una colisión de cupo")
                    .contains("RES-CUPO");
        }
        throw new AssertionError("Sin cupo tras 10 intentos por UI");
    }

    protected void crearPacienteViaApi(String rut) {
        crearPacienteViaApi(rut, "Paciente de Prueba UI", "PARTICULAR", null);
    }

    /** Paciente PARTICULAR con el nombre dado (por ejemplo, un nombre que contiene HTML). */
    protected void crearPacienteConNombreViaApi(String rut, String nombre) {
        crearPacienteViaApi(rut, nombre, "PARTICULAR", null);
    }

    /**
     * Paciente FONASA con el tramo dado: a diferencia de PARTICULAR, acá la bonificación
     * deja el copago por debajo del arancel bruto y permite comprobar que las
     * multas usan la base de cálculo correcta.
     */
    protected void crearPacienteFonasaViaApi(String rut, String tramo) {
        crearPacienteViaApi(rut, "Paciente de Prueba UI", "FONASA", tramo);
    }

    private void crearPacienteViaApi(String rut, String nombre, String prevision, String tramoFonasa) {
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
                      "nombre": "%s",
                      "fechaNacimiento": "1990-05-10",
                      "prevision": "%s",
                      "tramoFonasa": %s,
                      "convenioEmpresa": false
                    }
                    """.formatted(rut, nombre.replace("\"", "\\\""), prevision, tramoJson)));
            assertThat(respuesta.status()).as("creacion del paciente de prueba").isEqualTo(201);
        }
    }
}
