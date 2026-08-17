package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.aplicacion.CrearReservaCmd;
import cl.losandes.mediturno.reservas.aplicacion.ReservaService;
import cl.losandes.mediturno.reservas.dominio.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservas")
public class ReservaController {

    private final ReservaService servicio;

    public ReservaController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @PostMapping
    public ResponseEntity<ReservaResponse> crear(@Valid @RequestBody CrearReservaRequest req) {
        CrearReservaCmd cmd = new CrearReservaCmd(
                req.rutPaciente(),
                enumDe(Especialidad.class, req.especialidad(), "especialidad"),
                req.fechaHoraAtencion(),
                enumDe(Canal.class, req.canal(), "canal"),
                Boolean.TRUE.equals(req.aceptaListaEspera()));
        Reserva creada = servicio.crear(cmd);
        return ResponseEntity.status(HttpStatus.CREATED).body(ReservaResponse.de(creada));
    }

    @GetMapping
    public List<ReservaResponse> listar(@RequestParam(required = false) String rut) {
        return servicio.listar(rut).stream().map(ReservaResponse::de).toList();
    }

    @GetMapping("/{id}")
    public ReservaResponse detalle(@PathVariable Long id) {
        return ReservaResponse.de(servicio.exigir(id));
    }

    @PatchMapping("/{id}")
    public ReservaResponse modificar(@PathVariable Long id,
                                     @Valid @RequestBody ModificarReservaRequest req) {
        return ReservaResponse.de(servicio.modificar(id, req.fechaHoraAtencion()));
    }

    @PostMapping("/{id}/confirmar")
    public ReservaResponse confirmar(@PathVariable Long id) {
        return ReservaResponse.de(servicio.confirmar(id));
    }

    @PostMapping("/{id}/iniciar")
    public ReservaResponse iniciar(@PathVariable Long id) {
        return ReservaResponse.de(servicio.iniciar(id));
    }

    @PostMapping("/{id}/finalizar")
    public ReservaResponse finalizar(@PathVariable Long id) {
        return ReservaResponse.de(servicio.finalizar(id));
    }

    @PostMapping("/{id}/anular")
    public ReservaResponse anular(@PathVariable Long id) {
        return ReservaResponse.de(servicio.anular(id));
    }

    @PostMapping("/{id}/inasistencia")
    public ReservaResponse inasistencia(@PathVariable Long id) {
        return ReservaResponse.de(servicio.marcarInasistencia(id));
    }

    private static <E extends Enum<E>> E enumDe(Class<E> tipo, String valor, String campo) {
        try {
            return Enum.valueOf(tipo, valor.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("VAL-ENUM",
                    "Valor no admitido para " + campo + ": '" + valor + "'.");
        }
    }
}
