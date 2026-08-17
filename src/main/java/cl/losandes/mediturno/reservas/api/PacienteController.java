package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.aplicacion.ReservaService;
import cl.losandes.mediturno.reservas.dominio.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
public class PacienteController {

    private final ReservaService servicio;

    public PacienteController(ReservaService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public List<PacienteResponse> listar() {
        return servicio.pacientes().stream().map(PacienteResponse::de).toList();
    }

    @PostMapping
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody CrearPacienteRequest req) {
        TipoPrevision prevision;
        try {
            prevision = TipoPrevision.valueOf(req.prevision().trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("VAL-ENUM",
                    "Valor no admitido para prevision: '" + req.prevision() + "'.");
        }
        TramoFonasa tramo = null;
        if (req.tramoFonasa() != null && !req.tramoFonasa().isBlank()) {
            try {
                tramo = TramoFonasa.valueOf(req.tramoFonasa().trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ReglaNegocioException("VAL-ENUM",
                        "Valor no admitido para tramoFonasa: '" + req.tramoFonasa() + "'.");
            }
        }
        if (prevision == TipoPrevision.FONASA && tramo == null) {
            throw new ReglaNegocioException("VAL-TRAMO",
                    "Un paciente Fonasa requiere tramoFonasa.");
        }
        Paciente creado = servicio.registrarPaciente(new Paciente(req.rut().trim(), req.nombre().trim(),
                req.fechaNacimiento(), prevision, tramo, Boolean.TRUE.equals(req.convenioEmpresa())));
        return ResponseEntity.status(HttpStatus.CREATED).body(PacienteResponse.de(creado));
    }
}
