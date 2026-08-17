package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.aplicacion.RepositorioPacientes;
import cl.losandes.mediturno.reservas.dominio.Paciente;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class RepositorioPacientesJpa implements RepositorioPacientes {

    private final PacienteJpaRepository jpa;

    public RepositorioPacientesJpa(PacienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Paciente> porRut(String rut) {
        return jpa.findById(rut).map(Mapeador::aDominio);
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        jpa.save(Mapeador.aEntidad(paciente));
        return paciente;
    }

    @Override
    public List<Paciente> todos() {
        return jpa.findAll().stream().map(Mapeador::aDominio).toList();
    }
}
