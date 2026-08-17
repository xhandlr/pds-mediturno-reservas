package cl.losandes.mediturno.reservas.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record CrearReservaRequest(
        @NotBlank(message = "rutPaciente es obligatorio")
        @Size(max = 15, message = "rutPaciente admite a lo más 15 caracteres")
        String rutPaciente,

        @NotBlank(message = "especialidad es obligatoria")
        @Size(max = 30)
        String especialidad,

        @NotNull(message = "fechaHoraAtencion es obligatoria")
        LocalDateTime fechaHoraAtencion,

        @NotBlank(message = "canal es obligatorio")
        @Size(max = 20)
        String canal,

        Boolean aceptaListaEspera) {
}
