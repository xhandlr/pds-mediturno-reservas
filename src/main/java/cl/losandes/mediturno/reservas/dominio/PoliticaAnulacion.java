package cl.losandes.mediturno.reservas.dominio;

import java.time.Duration;
import java.time.LocalDateTime;

/** RN-RES-05, RN-RES-06 y RN-RES-07 — Multas por anulación e inasistencia. */
public final class PoliticaAnulacion {

    private static final long MINUTOS_SIN_COSTO = 240;
    private static final long MINUTOS_MULTA_MENOR = 60;

    private static final int PORCENTAJE_MULTA_MENOR = 30;
    private static final int PORCENTAJE_MULTA_MAYOR = 50;

    private PoliticaAnulacion() {
    }

    /** Porcentaje de multa aplicable a una anulación solicitada en {@code ahora}. */
    public static int porcentajeAnulacion(LocalDateTime fechaHoraAtencion, LocalDateTime ahora) {
        long minutos = Duration.between(ahora, fechaHoraAtencion).toMinutes();
        if (minutos > MINUTOS_SIN_COSTO) {
            return 0;
        }
        if (minutos >= MINUTOS_MULTA_MENOR) {
            return PORCENTAJE_MULTA_MENOR;
        }
        return PORCENTAJE_MULTA_MAYOR;
    }

    public static int porcentajeInasistencia() {
        return PORCENTAJE_MULTA_MAYOR;
    }

    /** Monto de la multa, en pesos enteros. */
    public static long calcularMulta(Reserva reserva, int porcentaje) {
        return reserva.arancelBruto() * porcentaje / 100;
    }
}
