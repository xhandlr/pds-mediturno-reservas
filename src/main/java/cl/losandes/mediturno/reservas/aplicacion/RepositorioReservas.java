package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RepositorioReservas {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> porId(Long id);

    List<Reserva> porPaciente(String rut);

    List<Reserva> enBloque(LocalDateTime fechaHora, Especialidad especialidad);

    List<Reserva> todas();
}
