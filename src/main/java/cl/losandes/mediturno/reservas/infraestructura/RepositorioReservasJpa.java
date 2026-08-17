package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.aplicacion.RepositorioReservas;
import cl.losandes.mediturno.reservas.dominio.Especialidad;
import cl.losandes.mediturno.reservas.dominio.Reserva;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioReservasJpa implements RepositorioReservas {

    private final ReservaJpaRepository jpa;

    public RepositorioReservasJpa(ReservaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Reserva guardar(Reserva reserva) {
        ReservaEntity guardada = jpa.save(Mapeador.aEntidad(reserva));
        reserva.asignarId(guardada.getId());
        return reserva;
    }

    @Override
    public Optional<Reserva> porId(Long id) {
        return jpa.findById(id).map(Mapeador::aDominio);
    }

    @Override
    public List<Reserva> porPaciente(String rut) {
        return jpa.findByRutPacienteOrderByFechaHoraAtencionAsc(rut).stream()
                .map(Mapeador::aDominio).toList();
    }

    @Override
    public List<Reserva> enBloque(LocalDateTime fechaHora, Especialidad especialidad) {
        return jpa.findByFechaHoraAtencionAndEspecialidad(fechaHora, especialidad.name()).stream()
                .map(Mapeador::aDominio).toList();
    }

    @Override
    public List<Reserva> todas() {
        return jpa.findAllByOrderByFechaHoraAtencionAsc().stream().map(Mapeador::aDominio).toList();
    }
}
