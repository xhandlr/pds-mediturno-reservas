package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.dominio.*;

/** Traduce entre el dominio puro y las entidades JPA. */
final class Mapeador {

    private Mapeador() {
    }

    static Paciente aDominio(PacienteEntity e) {
        return new Paciente(e.getRut(), e.getNombre(), e.getFechaNacimiento(),
                TipoPrevision.valueOf(e.getPrevision()),
                e.getTramoFonasa() == null || e.getTramoFonasa().isBlank()
                        ? null : TramoFonasa.valueOf(e.getTramoFonasa()),
                e.isConvenioEmpresa());
    }

    static PacienteEntity aEntidad(Paciente p) {
        return new PacienteEntity(p.rut(), p.nombre(), p.fechaNacimiento(),
                p.prevision().name(),
                p.tramoFonasa() == null ? null : p.tramoFonasa().name(),
                p.convenioEmpresa());
    }

    static Reserva aDominio(ReservaEntity e) {
        Reserva r = new Reserva(e.getRutPaciente(),
                Especialidad.valueOf(e.getEspecialidad()),
                e.getFechaHoraAtencion(),
                Canal.valueOf(e.getCanal()),
                new Tarifa(e.getArancelBruto(), e.getCopago()),
                e.getCreadaEn());
        r.restaurar(e.getId(), EstadoReserva.valueOf(e.getEstado()), e.getMulta(), e.getModificaciones());
        return r;
    }

    static ReservaEntity aEntidad(Reserva r) {
        return new ReservaEntity(r.id(), r.rutPaciente(), r.especialidad().name(), r.fechaHoraAtencion(),
                r.canal().name(), r.estado().name(), r.arancelBruto(), r.copago(), r.multa(),
                r.modificaciones(), r.creadaEn());
    }
}
