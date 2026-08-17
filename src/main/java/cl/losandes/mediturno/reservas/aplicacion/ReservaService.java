package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Orquesta las reglas RN-RES-01 a RN-RES-10 sobre el ciclo de vida de una reserva. */
public class ReservaService {

    public static final int MAX_RESERVAS_ACTIVAS = 3;
    public static final int MAX_ANTICIPACION_DIAS = 60;
    public static final int CUPOS_POR_BLOQUE = 1;
    public static final int INASISTENCIAS_PARA_BLOQUEO = 3;
    public static final int DIAS_DE_BLOQUEO = 30;

    private final RepositorioReservas reservas;
    private final RepositorioPacientes pacientes;
    private final TarificadorPort tarificador;
    private final Reloj reloj;

    public ReservaService(RepositorioReservas reservas, RepositorioPacientes pacientes,
                          TarificadorPort tarificador, Reloj reloj) {
        this.reservas = reservas;
        this.pacientes = pacientes;
        this.tarificador = tarificador;
        this.reloj = reloj;
    }

    // ------------------------------------------------------------------ crear

    public Reserva crear(CrearReservaCmd cmd) {
        LocalDateTime ahora = reloj.ahora();
        Paciente paciente = pacientes.porRut(cmd.rutPaciente())
                .orElseThrow(() -> new ReglaNegocioException("PAC-404",
                        "No existe un paciente con RUT " + cmd.rutPaciente() + "."));

        CalendarioAtencion.validarBloque(cmd.fechaHoraAtencion());
        validarAnticipacion(cmd.fechaHoraAtencion(), ahora);
        validarBloqueoPorInasistencias(paciente.rut(), cmd.canal(), ahora);
        validarTopeDeActivas(paciente.rut());
        validarDuplicadaEnElDia(paciente.rut(), cmd.especialidad(), cmd.fechaHoraAtencion());

        Tarifa tarifa = tarificador.tarificar(paciente, cmd.especialidad(), cmd.fechaHoraAtencion(), ahora);
        Reserva reserva = new Reserva(paciente.rut(), cmd.especialidad(), cmd.fechaHoraAtencion(),
                cmd.canal(), tarifa, ahora);

        if (bloqueOcupado(cmd.fechaHoraAtencion(), cmd.especialidad())) {
            if (!cmd.aceptaListaEspera()) {
                throw new ReglaNegocioException("RES-CUPO",
                        "El bloque solicitado ya no tiene cupo.");
            }
            reserva.dejarEnEspera();
        }
        return reservas.guardar(reserva);
    }

    /** RN-RES-02 — Ventana de reserva admitida. */
    private void validarAnticipacion(LocalDateTime fechaHora, LocalDateTime ahora) {
        if (!fechaHora.isAfter(ahora)) {
            throw new ReglaNegocioException("RES-02-PASADO",
                    "No se puede reservar una hora que ya pasó.");
        }
        long dias = ChronoUnit.DAYS.between(ahora.toLocalDate(), fechaHora.toLocalDate());
        if (dias > MAX_ANTICIPACION_DIAS + 1) {
            throw new ReglaNegocioException("RES-02-ANTICIPACION",
                    "No se puede reservar con más de " + MAX_ANTICIPACION_DIAS + " días de anticipación.");
        }
    }

    /** RN-RES-03 — Tope de reservas activas simultáneas. */
    private void validarTopeDeActivas(String rut) {
        long activas = reservas.porPaciente(rut).stream()
                .filter(r -> r.estado().esActivo())
                .count();
        if (activas > MAX_RESERVAS_ACTIVAS) {
            throw new ReglaNegocioException("RES-03-TOPE",
                    "El paciente ya tiene el máximo de " + MAX_RESERVAS_ACTIVAS + " reservas activas.");
        }
    }

    /** RN-RES-04 — Una sola reserva por especialidad y día. */
    private void validarDuplicadaEnElDia(String rut, Especialidad especialidad, LocalDateTime fechaHora) {
        boolean duplicada = reservas.porPaciente(rut).stream()
                .filter(r -> r.estado().esActivo())
                .filter(r -> r.especialidad() == especialidad)
                .anyMatch(r -> r.fechaHoraAtencion().equals(fechaHora));
        if (duplicada) {
            throw new ReglaNegocioException("RES-04-DUPLICADA",
                    "El paciente ya tiene una reserva de " + especialidad + " para ese día.");
        }
    }

    /** RN-RES-08 — Bloqueo de reserva en línea por inasistencias reiteradas. */
    private void validarBloqueoPorInasistencias(String rut, Canal canal, LocalDateTime ahora) {
        if (canal != Canal.WEB && canal != Canal.APP_MOVIL) {
            return;
        }
        LocalDateTime desde = ahora.withDayOfYear(1).toLocalDate().atStartOfDay();
        List<Reserva> inasistencias = reservas.porPaciente(rut).stream()
                .filter(r -> r.estado() == EstadoReserva.NO_ASISTIDA)
                .filter(r -> !r.fechaHoraAtencion().isBefore(desde))
                .sorted(Comparator.comparing(Reserva::fechaHoraAtencion))
                .toList();
        if (inasistencias.size() < INASISTENCIAS_PARA_BLOQUEO) {
            return;
        }
        LocalDateTime ultima = inasistencias.get(inasistencias.size() - 1).fechaHoraAtencion();
        if (ahora.isBefore(ultima.plusDays(DIAS_DE_BLOQUEO))) {
            throw new ReglaNegocioException("RES-08-BLOQUEO",
                    "El paciente está bloqueado para reservar en línea por inasistencias reiteradas.");
        }
    }

    private boolean bloqueOcupado(LocalDateTime fechaHora, Especialidad especialidad) {
        long ocupados = reservas.enBloque(fechaHora, especialidad).stream()
                .filter(r -> r.estado().esActivo())
                .count();
        return ocupados >= CUPOS_POR_BLOQUE;
    }

    // ------------------------------------------------------------ transiciones

    public Reserva confirmar(Long id) {
        Reserva r = exigir(id);
        r.confirmar();
        return reservas.guardar(r);
    }

    public Reserva iniciar(Long id) {
        Reserva r = exigir(id);
        r.iniciar();
        return reservas.guardar(r);
    }

    public Reserva finalizar(Long id) {
        Reserva r = exigir(id);
        r.finalizar();
        return reservas.guardar(r);
    }

    public Reserva anular(Long id) {
        Reserva r = exigir(id);
        r.anular(reloj.ahora());
        Reserva guardada = reservas.guardar(r);
        promoverListaDeEspera(guardada);
        return guardada;
    }

    public Reserva marcarInasistencia(Long id) {
        Reserva r = exigir(id);
        r.marcarInasistencia();
        return reservas.guardar(r);
    }

    /** RN-RES-09 — Cambio de hora de una reserva existente. */
    public Reserva modificar(Long id, LocalDateTime nuevaFechaHora) {
        Reserva r = exigir(id);
        LocalDateTime ahora = reloj.ahora();
        CalendarioAtencion.validarBloque(nuevaFechaHora);
        validarAnticipacion(nuevaFechaHora, ahora);
        if (bloqueOcupado(nuevaFechaHora, r.especialidad())) {
            throw new ReglaNegocioException("RES-CUPO", "El bloque solicitado ya no tiene cupo.");
        }
        Paciente paciente = pacientes.porRut(r.rutPaciente()).orElseThrow();
        Tarifa tarifa = tarificador.tarificar(paciente, r.especialidad(), nuevaFechaHora, ahora);
        r.mover(nuevaFechaHora, tarifa);
        return reservas.guardar(r);
    }

    /** Al liberarse un cupo, promueve la primera reserva EN_ESPERA del mismo bloque. */
    private void promoverListaDeEspera(Reserva liberada) {
        reservas.enBloque(liberada.fechaHoraAtencion(), liberada.especialidad()).stream()
                .filter(r -> r.estado() == EstadoReserva.EN_ESPERA)
                .min(Comparator.comparing(Reserva::creadaEn))
                .ifPresent(r -> {
                    r.confirmar();
                    reservas.guardar(r);
                });
    }

    // ---------------------------------------------------------------- consultas

    public Reserva exigir(Long id) {
        return reservas.porId(id).orElseThrow(() -> new ReglaNegocioException("RES-404",
                "No existe la reserva " + id + "."));
    }

    public Optional<Reserva> porId(Long id) {
        return reservas.porId(id);
    }

    public List<Reserva> listar(String rutPaciente) {
        return rutPaciente == null || rutPaciente.isBlank()
                ? reservas.todas()
                : reservas.porPaciente(rutPaciente);
    }

    public List<Paciente> pacientes() {
        return pacientes.todos();
    }

    public Paciente registrarPaciente(Paciente paciente) {
        if (pacientes.porRut(paciente.rut()).isPresent()) {
            throw new ReglaNegocioException("PAC-DUPLICADO",
                    "Ya existe un paciente con RUT " + paciente.rut() + ".");
        }
        return pacientes.guardar(paciente);
    }

    public LocalDateTime ahora() {
        return reloj.ahora();
    }
}
