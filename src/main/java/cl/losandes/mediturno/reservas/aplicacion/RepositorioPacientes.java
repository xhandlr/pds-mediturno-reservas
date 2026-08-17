package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.util.List;
import java.util.Optional;

public interface RepositorioPacientes {

    Optional<Paciente> porRut(String rut);

    Paciente guardar(Paciente paciente);

    List<Paciente> todos();
}
