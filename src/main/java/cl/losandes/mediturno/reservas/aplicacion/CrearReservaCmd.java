package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.time.LocalDateTime;

/**
 * Datos de entrada para crear una reserva.
 *
 * @param aceptaListaEspera si el bloque está ocupado, deja la reserva EN_ESPERA
 *                          en vez de rechazarla
 */
public record CrearReservaCmd(String rutPaciente,
                              Especialidad especialidad,
                              LocalDateTime fechaHoraAtencion,
                              Canal canal,
                              boolean aceptaListaEspera) {
}
