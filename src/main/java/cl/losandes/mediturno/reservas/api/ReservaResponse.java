package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.dominio.Reserva;

import java.time.LocalDateTime;

public record ReservaResponse(Long id,
                              String rutPaciente,
                              String especialidad,
                              LocalDateTime fechaHoraAtencion,
                              String canal,
                              String estado,
                              long arancelBruto,
                              long copago,
                              long multa,
                              int modificaciones) {

    public static ReservaResponse de(Reserva r) {
        return new ReservaResponse(r.id(), r.rutPaciente(), r.especialidad().name(),
                r.fechaHoraAtencion(), r.canal().name(), r.estado().name(),
                r.arancelBruto(), r.copago(), r.multa(), r.modificaciones());
    }
}
