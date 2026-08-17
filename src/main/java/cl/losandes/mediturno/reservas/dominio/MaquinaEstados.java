package cl.losandes.mediturno.reservas.dominio;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static cl.losandes.mediturno.reservas.dominio.EstadoReserva.*;

/**
 * RN-RES-10 — Transiciones admitidas entre estados.
 *
 * <p>Esta tabla es la implementación vigente. El enunciado del curso describe el
 * camino principal y nombra los estados alternos, pero no enumera todas las
 * transiciones: las que aquí aparecen y el enunciado no menciona son decisiones
 * del equipo de desarrollo.</p>
 */
public final class MaquinaEstados {

    private static final Map<EstadoReserva, Set<EstadoReserva>> TRANSICIONES =
            new EnumMap<>(EstadoReserva.class);

    static {
        TRANSICIONES.put(BORRADOR, EnumSet.of(CONFIRMADA, EN_ESPERA, ANULADA));
        TRANSICIONES.put(EN_ESPERA, EnumSet.of(CONFIRMADA, ANULADA));
        TRANSICIONES.put(CONFIRMADA, EnumSet.of(EN_CURSO, ANULADA, NO_ASISTIDA));
        TRANSICIONES.put(EN_CURSO, EnumSet.of(ATENDIDA, ANULADA));
        TRANSICIONES.put(ATENDIDA, EnumSet.of(ANULADA));
        TRANSICIONES.put(ANULADA, EnumSet.noneOf(EstadoReserva.class));
        TRANSICIONES.put(NO_ASISTIDA, EnumSet.noneOf(EstadoReserva.class));
    }

    private MaquinaEstados() {
    }

    public static boolean permitida(EstadoReserva origen, EstadoReserva destino) {
        return TRANSICIONES.getOrDefault(origen, EnumSet.noneOf(EstadoReserva.class)).contains(destino);
    }

    public static void validar(EstadoReserva origen, EstadoReserva destino) {
        if (!permitida(origen, destino)) {
            throw new ReglaNegocioException("RES-10-TRANSICION",
                    "Transición no permitida: " + origen + " -> " + destino + ".");
        }
    }

    public static Set<EstadoReserva> destinosDesde(EstadoReserva origen) {
        return EnumSet.copyOf(TRANSICIONES.getOrDefault(origen, EnumSet.noneOf(EstadoReserva.class)));
    }
}
