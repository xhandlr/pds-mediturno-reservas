package cl.losandes.mediturno.reservas.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "paciente")
public class PacienteEntity {

    @Id
    @Column(length = 15)
    private String rut;

    @Column(nullable = false, length = 120)
    private String nombre;

    @Column(nullable = false)
    private LocalDate fechaNacimiento;

    @Column(nullable = false, length = 20)
    private String prevision;

    @Column(length = 2)
    private String tramoFonasa;

    @Column(nullable = false)
    private boolean convenioEmpresa;

    protected PacienteEntity() {
    }

    public PacienteEntity(String rut, String nombre, LocalDate fechaNacimiento,
                          String prevision, String tramoFonasa, boolean convenioEmpresa) {
        this.rut = rut;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.prevision = prevision;
        this.tramoFonasa = tramoFonasa;
        this.convenioEmpresa = convenioEmpresa;
    }

    public String getRut() { return rut; }
    public String getNombre() { return nombre; }
    public LocalDate getFechaNacimiento() { return fechaNacimiento; }
    public String getPrevision() { return prevision; }
    public String getTramoFonasa() { return tramoFonasa; }
    public boolean isConvenioEmpresa() { return convenioEmpresa; }
}
