package cl.losandes.mediturno.reservas.dominio;

/**
 * RN-RES-10 — Estados del ciclo de vida de una reserva.
 *
 * <p>El enunciado describe el camino principal
 * {@code BORRADOR -> CONFIRMADA -> EN_CURSO -> ATENDIDA} y menciona las salidas
 * alternas {@code ANULADA}, {@code NO_ASISTIDA} y {@code EN_ESPERA}, pero no
 * especifica todas las transiciones. Las decisiones tomadas aquí son las del
 * equipo de desarrollo, no las de la especificación.</p>
 */
public enum EstadoReserva {

    BORRADOR,
    EN_ESPERA,
    CONFIRMADA,
    EN_CURSO,
    ATENDIDA,
    ANULADA,
    NO_ASISTIDA;

    /** Estados que ocupan cupo y cuentan para el tope de RN-RES-03. */
    public boolean esActivo() {
        return this == BORRADOR || this == EN_ESPERA || this == CONFIRMADA || this == EN_CURSO;
    }

    public boolean esTerminal() {
        return this == ATENDIDA || this == ANULADA || this == NO_ASISTIDA;
    }
}
