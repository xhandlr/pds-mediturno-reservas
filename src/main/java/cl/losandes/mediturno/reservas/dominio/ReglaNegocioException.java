package cl.losandes.mediturno.reservas.dominio;

/**
 * Rechazo por incumplimiento de una regla de negocio.
 *
 * <p>El código es parte del contrato público de la API: las pruebas de caja negra
 * se apoyan en él y no en el texto del mensaje, que puede cambiar.</p>
 */
public class ReglaNegocioException extends RuntimeException {

    private final String codigo;

    public ReglaNegocioException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String codigo() {
        return codigo;
    }
}
