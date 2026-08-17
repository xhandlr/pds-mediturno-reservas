package cl.losandes.mediturno.reservas.infraestructura;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservaJpaRepository extends JpaRepository<ReservaEntity, Long> {

    List<ReservaEntity> findByRutPacienteOrderByFechaHoraAtencionAsc(String rutPaciente);

    List<ReservaEntity> findByFechaHoraAtencionAndEspecialidad(LocalDateTime fechaHoraAtencion,
                                                              String especialidad);

    List<ReservaEntity> findAllByOrderByFechaHoraAtencionAsc();
}
