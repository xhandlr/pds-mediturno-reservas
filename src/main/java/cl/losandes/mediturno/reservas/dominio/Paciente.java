package cl.losandes.mediturno.reservas.dominio;

import java.time.LocalDate;

/**
 * Paciente registrado en MediTurno.
 *
 * @param rut              RUT con dígito verificador, formato 12.345.678-9
 * @param nombre           nombre completo, solo para la interfaz
 * @param fechaNacimiento  determina la edad a la fecha de atención
 * @param prevision        sistema previsional
 * @param tramoFonasa      solo aplica cuando la previsión es FONASA; puede ser null
 * @param convenioEmpresa  true si el paciente tiene convenio de empresa vigente
 */
public record Paciente(String rut,
                       String nombre,
                       LocalDate fechaNacimiento,
                       TipoPrevision prevision,
                       TramoFonasa tramoFonasa,
                       boolean convenioEmpresa) {

    public int edadA(LocalDate fecha) {
        return java.time.Period.between(fechaNacimiento, fecha).getYears();
    }
}
