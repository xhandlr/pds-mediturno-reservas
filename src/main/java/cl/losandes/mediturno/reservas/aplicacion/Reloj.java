package cl.losandes.mediturno.reservas.aplicacion;

import java.time.LocalDateTime;

/** Fuente de la hora. Permite que las reglas dependientes del tiempo sean deterministas. */
public interface Reloj {
    LocalDateTime ahora();
}
