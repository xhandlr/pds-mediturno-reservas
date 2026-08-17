package cl.losandes.mediturno.reservas.dominio;

/** RN-TAR-02 — Porcentaje de bonificación según el tramo Fonasa del paciente. */
public enum TramoFonasa {

    A(100),
    B(90),
    C(80),
    D(70);

    private final int porcentajeBonificacion;

    TramoFonasa(int porcentajeBonificacion) {
        this.porcentajeBonificacion = porcentajeBonificacion;
    }

    public int porcentajeBonificacion() {
        return porcentajeBonificacion;
    }
}
