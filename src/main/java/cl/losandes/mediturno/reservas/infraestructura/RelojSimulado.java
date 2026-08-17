package cl.losandes.mediturno.reservas.infraestructura;

import java.time.LocalDateTime;

/**
 * Permite fijar la hora del servicio para una petición concreta.
 *
 * <p>Es una facilidad de prueba deliberada: sin ella, las reglas que dependen del
 * tiempo (RN-RES-02, RN-RES-05 a RN-RES-08) solo se pueden ejercitar esperando. El
 * valor es por petición y por hilo, de modo que la suite puede ejecutarse en
 * paralelo sin interferencias.</p>
 */
public final class RelojSimulado {

    public static final String CABECERA = "X-Reloj-Simulado";

    private static final ThreadLocal<LocalDateTime> VALOR = new ThreadLocal<>();

    private RelojSimulado() {
    }

    public static void fijar(LocalDateTime t) {
        VALOR.set(t);
    }

    public static LocalDateTime vigente() {
        return VALOR.get();
    }

    public static void limpiar() {
        VALOR.remove();
    }
}
