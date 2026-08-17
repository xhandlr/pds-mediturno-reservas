package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.dominio.Paciente;

import java.time.LocalDate;

public record PacienteResponse(String rut,
                               String nombre,
                               LocalDate fechaNacimiento,
                               String prevision,
                               String tramoFonasa,
                               boolean convenioEmpresa) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.rut(), p.nombre(), p.fechaNacimiento(),
                p.prevision().name(),
                p.tramoFonasa() == null ? null : p.tramoFonasa().name(),
                p.convenioEmpresa());
    }
}
