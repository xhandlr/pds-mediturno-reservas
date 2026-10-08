package cl.losandes.mediturno.reservas.ui.paginas;

import com.microsoft.playwright.Page;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Página "/reservas/nueva": formulario de creación de reserva. */
public class NuevaReservaPage extends BasePage {

    private static final DateTimeFormatter FORMATO_FECHA =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    public NuevaReservaPage(Page page, String baseUrl) {
        super(page, baseUrl);
    }

    public NuevaReservaPage navegar() {
        page.navigate(baseUrl + "/reservas/nueva");
        testid("form-reserva").waitFor();
        return this;
    }

    public NuevaReservaPage ingresarRut(String rut) {
        testid("input-rut").fill(rut);
        return this;
    }

    public NuevaReservaPage seleccionarEspecialidad(String especialidad) {
        testid("select-especialidad").selectOption(especialidad);
        return this;
    }

    public NuevaReservaPage ingresarFechaHora(LocalDateTime fechaHora) {
        testid("input-fecha").fill(fechaHora.format(FORMATO_FECHA));
        return this;
    }

    public NuevaReservaPage seleccionarCanal(String canal) {
        testid("select-canal").selectOption(canal);
        return this;
    }

    public NuevaReservaPage aceptarListaDeEspera() {
        testid("check-lista-espera").check();
        return this;
    }

    /** Llena el formulario completo y lo envía. */
    public void crearReserva(String rut, String especialidad, LocalDateTime fechaHora, String canal,
                              boolean aceptaListaEspera) {
        ingresarRut(rut);
        seleccionarEspecialidad(especialidad);
        ingresarFechaHora(fechaHora);
        seleccionarCanal(canal);
        if (aceptaListaEspera) {
            aceptarListaDeEspera();
        }
        testid("btn-crear").click();
    }
}
