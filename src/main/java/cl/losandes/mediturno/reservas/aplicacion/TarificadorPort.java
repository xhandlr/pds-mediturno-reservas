package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.time.LocalDateTime;

/**
 * Cálculo del arancel y del copago de una atención.
 *
 * <p>En esta versión el cálculo es local. A partir del Release 2.1 la
 * implementación pasa a ser un cliente HTTP de {@code ms-tarificacion}.</p>
 */
public interface TarificadorPort {

    Tarifa tarificar(Paciente paciente, Especialidad especialidad,
                     LocalDateTime fechaHoraAtencion, LocalDateTime ahora);
}
