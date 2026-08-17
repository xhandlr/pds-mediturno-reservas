package cl.losandes.mediturno.reservas.infraestructura;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "reserva")
public class ReservaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 15)
    private String rutPaciente;

    @Column(nullable = false, length = 30)
    private String especialidad;

    @Column(nullable = false)
    private LocalDateTime fechaHoraAtencion;

    @Column(nullable = false, length = 20)
    private String canal;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(nullable = false)
    private long arancelBruto;

    @Column(nullable = false)
    private long copago;

    @Column(nullable = false)
    private long multa;

    @Column(nullable = false)
    private int modificaciones;

    @Column(nullable = false)
    private LocalDateTime creadaEn;

    protected ReservaEntity() {
    }

    public ReservaEntity(Long id, String rutPaciente, String especialidad, LocalDateTime fechaHoraAtencion,
                         String canal, String estado, long arancelBruto, long copago, long multa,
                         int modificaciones, LocalDateTime creadaEn) {
        this.id = id;
        this.rutPaciente = rutPaciente;
        this.especialidad = especialidad;
        this.fechaHoraAtencion = fechaHoraAtencion;
        this.canal = canal;
        this.estado = estado;
        this.arancelBruto = arancelBruto;
        this.copago = copago;
        this.multa = multa;
        this.modificaciones = modificaciones;
        this.creadaEn = creadaEn;
    }

    public Long getId() { return id; }
    public String getRutPaciente() { return rutPaciente; }
    public String getEspecialidad() { return especialidad; }
    public LocalDateTime getFechaHoraAtencion() { return fechaHoraAtencion; }
    public String getCanal() { return canal; }
    public String getEstado() { return estado; }
    public long getArancelBruto() { return arancelBruto; }
    public long getCopago() { return copago; }
    public long getMulta() { return multa; }
    public int getModificaciones() { return modificaciones; }
    public LocalDateTime getCreadaEn() { return creadaEn; }
}
