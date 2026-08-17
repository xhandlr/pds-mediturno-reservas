package cl.losandes.mediturno.reservas.api;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ModificarReservaRequest(
        @NotNull(message = "fechaHoraAtencion es obligatoria")
        LocalDateTime fechaHoraAtencion) {
}
