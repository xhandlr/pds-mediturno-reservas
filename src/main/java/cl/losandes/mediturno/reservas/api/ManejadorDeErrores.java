package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.dominio.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Traduce las excepciones a la representación pública de error.
 *
 * <p>El código de negocio es estable y forma parte del contrato; el mensaje es
 * informativo y puede cambiar entre versiones.</p>
 */
@RestControllerAdvice
public class ManejadorDeErrores {

    private static final Map<String, HttpStatus> ESTADOS = Map.ofEntries(
            Map.entry("RES-01-DIA", HttpStatus.BAD_REQUEST),
            Map.entry("RES-01-BLOQUE", HttpStatus.BAD_REQUEST),
            Map.entry("RES-01-HORARIO", HttpStatus.BAD_REQUEST),
            Map.entry("RES-02-PASADO", HttpStatus.BAD_REQUEST),
            Map.entry("RES-02-ANTICIPACION", HttpStatus.BAD_REQUEST),
            Map.entry("RES-03-TOPE", HttpStatus.CONFLICT),
            Map.entry("RES-04-DUPLICADA", HttpStatus.CONFLICT),
            Map.entry("RES-08-BLOQUEO", HttpStatus.CONFLICT),
            Map.entry("RES-09-TOPE", HttpStatus.CONFLICT),
            Map.entry("RES-09-ESTADO", HttpStatus.CONFLICT),
            Map.entry("RES-10-TRANSICION", HttpStatus.CONFLICT),
            Map.entry("RES-CUPO", HttpStatus.CONFLICT),
            Map.entry("RES-404", HttpStatus.NOT_FOUND),
            Map.entry("PAC-404", HttpStatus.NOT_FOUND),
            Map.entry("PAC-DUPLICADO", HttpStatus.CONFLICT),
            Map.entry("VAL-ENUM", HttpStatus.BAD_REQUEST),
            Map.entry("VAL-TRAMO", HttpStatus.BAD_REQUEST));

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ErrorResponse> regla(ReglaNegocioException e, HttpServletRequest req) {
        HttpStatus estado = ESTADOS.getOrDefault(e.codigo(), HttpStatus.UNPROCESSABLE_ENTITY);
        return construir(e.codigo(), e.getMessage(), estado, req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> validacion(MethodArgumentNotValidException e,
                                                    HttpServletRequest req) {
        String detalle = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return construir("VAL-CAMPOS", detalle, HttpStatus.BAD_REQUEST, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> cuerpoIlegible(HttpMessageNotReadableException e,
                                                        HttpServletRequest req) {
        return construir("VAL-JSON", "El cuerpo de la petición no es un JSON válido o "
                + "algún campo tiene un tipo incorrecto.", HttpStatus.BAD_REQUEST, req);
    }

    private ResponseEntity<ErrorResponse> construir(String codigo, String mensaje,
                                                    HttpStatus estado, HttpServletRequest req) {
        return ResponseEntity.status(estado).body(new ErrorResponse(codigo, mensaje,
                estado.value(), req.getRequestURI(), LocalDateTime.now()));
    }
}
