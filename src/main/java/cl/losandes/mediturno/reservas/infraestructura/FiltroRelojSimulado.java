package cl.losandes.mediturno.reservas.infraestructura;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

/** Lee la cabecera {@code X-Reloj-Simulado} y la deja disponible durante la petición. */
@Component
public class FiltroRelojSimulado implements Filter {

    @Value("${mediturno.reloj-simulado.habilitado:true}")
    private boolean habilitado;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String valor = habilitado && request instanceof HttpServletRequest http
                ? http.getHeader(RelojSimulado.CABECERA) : null;
        try {
            if (valor != null && !valor.isBlank()) {
                try {
                    RelojSimulado.fijar(LocalDateTime.parse(valor.trim()));
                } catch (DateTimeParseException e) {
                    ((HttpServletResponse) response).sendError(HttpServletResponse.SC_BAD_REQUEST,
                            "Cabecera " + RelojSimulado.CABECERA + " con formato inválido. "
                                    + "Se espera ISO-8601, por ejemplo 2026-10-05T09:00:00.");
                    return;
                }
            }
            chain.doFilter(request, response);
        } finally {
            RelojSimulado.limpiar();
        }
    }
}
