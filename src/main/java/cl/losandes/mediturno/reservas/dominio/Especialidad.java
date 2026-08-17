package cl.losandes.mediturno.reservas.dominio;

/** RN-TAR-01 — Arancel base por especialidad, en pesos chilenos. */
public enum Especialidad {

    MEDICINA_GENERAL(25_000),
    NUTRICION(28_000),
    PEDIATRIA(32_000),
    CARDIOLOGIA(48_000),
    PSIQUIATRIA(55_000);

    private final long arancelBase;

    Especialidad(long arancelBase) {
        this.arancelBase = arancelBase;
    }

    public long arancelBase() {
        return arancelBase;
    }
}
