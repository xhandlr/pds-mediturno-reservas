package cl.losandes.mediturno.reservas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CrearPacienteRequest(
        @NotBlank @Size(max = 15) String rut,
        @NotBlank @Size(max = 120) String nombre,
        @NotNull LocalDate fechaNacimiento,
        @NotBlank @Size(max = 20) String prevision,
        @Size(max = 2) String tramoFonasa,
        Boolean convenioEmpresa) {
}
