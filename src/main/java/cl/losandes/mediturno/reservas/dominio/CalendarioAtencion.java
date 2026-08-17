package cl.losandes.mediturno.reservas.dominio;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

/** RN-RES-01 — Bloques de atención disponibles. */
public final class CalendarioAtencion {

    public static final int DURACION_BLOQUE_MINUTOS = 20;
    private static final int HORA_APERTURA = 8;
    private static final int HORA_CIERRE = 18;

    private CalendarioAtencion() {
    }

    /** Lanza {@link ReglaNegocioException} si la hora pedida no es un bloque válido. */
    public static void validarBloque(LocalDateTime fechaHora) {
        DayOfWeek dia = fechaHora.getDayOfWeek();
        if (dia == DayOfWeek.SUNDAY) {
            throw new ReglaNegocioException("RES-01-DIA",
                    "No se atiende los domingos.");
        }
        if (fechaHora.getMinute() % DURACION_BLOQUE_MINUTOS != 0
                || fechaHora.getSecond() != 0 || fechaHora.getNano() != 0) {
            throw new ReglaNegocioException("RES-01-BLOQUE",
                    "La atención debe comenzar en un bloque de " + DURACION_BLOQUE_MINUTOS + " minutos.");
        }
        int hora = fechaHora.getHour();
        if (hora < HORA_APERTURA || hora >= HORA_CIERRE) {
            throw new ReglaNegocioException("RES-01-HORARIO",
                    "Fuera del horario de atención.");
        }
    }

    public static boolean esBloqueValido(LocalDateTime fechaHora) {
        try {
            validarBloque(fechaHora);
            return true;
        } catch (ReglaNegocioException e) {
            return false;
        }
    }
}
