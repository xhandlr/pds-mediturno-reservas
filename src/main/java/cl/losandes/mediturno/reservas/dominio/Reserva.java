package cl.losandes.mediturno.reservas.dominio;

import java.time.LocalDateTime;

/** Reserva de una hora médica. Concentra el estado y las reglas que dependen de él. */
public class Reserva {

    public static final int MAX_MODIFICACIONES = 2;

    private Long id;
    private final String rutPaciente;
    private final Especialidad especialidad;
    private final Canal canal;
    private final LocalDateTime creadaEn;

    private LocalDateTime fechaHoraAtencion;
    private EstadoReserva estado;
    private long arancelBruto;
    private long copago;
    private long multa;
    private int modificaciones;

    public Reserva(String rutPaciente, Especialidad especialidad, LocalDateTime fechaHoraAtencion,
                   Canal canal, Tarifa tarifa, LocalDateTime creadaEn) {
        this.rutPaciente = rutPaciente;
        this.especialidad = especialidad;
        this.fechaHoraAtencion = fechaHoraAtencion;
        this.canal = canal;
        this.arancelBruto = tarifa.arancelBruto();
        this.copago = tarifa.copago();
        this.creadaEn = creadaEn;
        this.estado = EstadoReserva.BORRADOR;
        this.multa = 0L;
        this.modificaciones = 0;
    }

    // ------------------------------------------------------------ transiciones

    public void confirmar() {
        MaquinaEstados.validar(estado, EstadoReserva.CONFIRMADA);
        estado = EstadoReserva.CONFIRMADA;
    }

    public void dejarEnEspera() {
        MaquinaEstados.validar(estado, EstadoReserva.EN_ESPERA);
        estado = EstadoReserva.EN_ESPERA;
    }

    public void iniciar() {
        MaquinaEstados.validar(estado, EstadoReserva.EN_CURSO);
        estado = EstadoReserva.EN_CURSO;
    }

    public void finalizar() {
        MaquinaEstados.validar(estado, EstadoReserva.ATENDIDA);
        estado = EstadoReserva.ATENDIDA;
    }

    /** RN-RES-05 a RN-RES-07: la anulación puede generar multa. */
    public void anular(LocalDateTime ahora) {
        MaquinaEstados.validar(estado, EstadoReserva.ANULADA);
        int porcentaje = PoliticaAnulacion.porcentajeAnulacion(fechaHoraAtencion, ahora);
        multa = PoliticaAnulacion.calcularMulta(this, porcentaje);
        estado = EstadoReserva.ANULADA;
    }

    public void marcarInasistencia() {
        MaquinaEstados.validar(estado, EstadoReserva.NO_ASISTIDA);
        multa = PoliticaAnulacion.calcularMulta(this, PoliticaAnulacion.porcentajeInasistencia());
        estado = EstadoReserva.NO_ASISTIDA;
    }

    /** RN-RES-09 — Cambio de hora. */
    public void mover(LocalDateTime nuevaFechaHora, Tarifa nuevaTarifa) {
        if (estado != EstadoReserva.BORRADOR && estado != EstadoReserva.CONFIRMADA) {
            throw new ReglaNegocioException("RES-09-ESTADO",
                    "Solo se puede modificar una reserva en BORRADOR o CONFIRMADA.");
        }
        if (modificaciones >= MAX_MODIFICACIONES) {
            throw new ReglaNegocioException("RES-09-TOPE",
                    "La reserva alcanzó el máximo de " + MAX_MODIFICACIONES
                            + " modificaciones. Debe anularla y crear una nueva.");
        }
        if (!nuevaFechaHora.toLocalDate().equals(fechaHoraAtencion.toLocalDate())) {
            modificaciones++;
        }
        fechaHoraAtencion = nuevaFechaHora;
        arancelBruto = nuevaTarifa.arancelBruto();
        copago = nuevaTarifa.copago();
    }

    // ---------------------------------------------------------------- accesores

    public Long id() { return id; }
    public void asignarId(Long id) { this.id = id; }
    public String rutPaciente() { return rutPaciente; }
    public Especialidad especialidad() { return especialidad; }
    public Canal canal() { return canal; }
    public LocalDateTime fechaHoraAtencion() { return fechaHoraAtencion; }
    public LocalDateTime creadaEn() { return creadaEn; }
    public EstadoReserva estado() { return estado; }
    public long arancelBruto() { return arancelBruto; }
    public long copago() { return copago; }
    public long multa() { return multa; }
    public int modificaciones() { return modificaciones; }

    /** Reconstrucción desde la base de datos. No aplica reglas. */
    public void restaurar(Long id, EstadoReserva estado, long multa, int modificaciones) {
        this.id = id;
        this.estado = estado;
        this.multa = multa;
        this.modificaciones = modificaciones;
    }
}
