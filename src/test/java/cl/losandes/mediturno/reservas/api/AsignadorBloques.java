package cl.losandes.mediturno.reservas.api;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Entrega bloques horarios únicos dentro de la JVM (seguro entre hilos). Entre corridas la base
 * es persistente, así que igual puede haber choques: ClienteReservas reintenta ante RES-CUPO.
 *
 * Bloques válidos: lunes a viernes (se evita sábado, DEF-01), 08:00 a 17:40 cada 20 minutos,
 * entre 2 y 55 días después de la fecha base (se evita el borde de 61 días, DEF-02).
 */
public final class AsignadorBloques {

    private static final Set<LocalDateTime> USADOS = ConcurrentHashMap.newKeySet();

    private AsignadorBloques() {
    }

    public static LocalDateTime siguiente(LocalDateTime baseAhora) {
        ThreadLocalRandom azar = ThreadLocalRandom.current();
        while (true) {
            LocalDate dia = baseAhora.toLocalDate().plusDays(azar.nextInt(2, 56));
            if (dia.getDayOfWeek() == DayOfWeek.SATURDAY || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
                continue;
            }
            int hora = 8 + azar.nextInt(10);          // 08..17
            int minuto = azar.nextInt(3) * 20;        // 00, 20, 40
            LocalDateTime bloque = dia.atTime(hora, minuto);
            if (USADOS.add(bloque)) {
                return bloque;
            }
        }
    }
}
