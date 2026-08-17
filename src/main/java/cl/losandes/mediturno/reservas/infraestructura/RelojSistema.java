package cl.losandes.mediturno.reservas.infraestructura;

import cl.losandes.mediturno.reservas.aplicacion.Reloj;

import java.time.LocalDateTime;

/** Hora del sistema, con la posibilidad de ser sustituida por petición. */
public class RelojSistema implements Reloj {

    @Override
    public LocalDateTime ahora() {
        LocalDateTime simulada = RelojSimulado.vigente();
        return simulada != null ? simulada : LocalDateTime.now();
    }
}
