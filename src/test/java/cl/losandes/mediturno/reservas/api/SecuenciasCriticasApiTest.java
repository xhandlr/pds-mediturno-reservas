package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

// Secuencias de dos transiciones (1-switch, issue #19). Cada test afirma ambos pasos: respuesta,
// esquema y consulta posterior de estado, multa y modificaciones. Los montos se calculan sobre el
// copago que devuelve la API (patron CP-28). Con pacientes PARTICULAR el copago coincide con el
// arancel, lo que enmascara DEF-06 (lo cubre CP-04).
class SecuenciasCriticasApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";
    private static final int MAX_INTENTOS = 10;
    private static final int MAX_SORTEOS = 1000;

    /** Primera reserva (BORRADOR) y segunda en el mismo bloque, que queda EN_ESPERA. */
    private record Escenario(ReservaCreada primera, long idSegunda) { }

    private record Modificacion(Response respuesta, LocalDateTime nuevaFechaHora) { }

    // ---------------------------------------------------------------- ayudas

    private static long crearEnEspera(LocalDateTime bloque) {
        String rut = ClienteReservas.crearPaciente(PerfilPaciente.particular());
        Response r = ClienteReservas.crearReserva(rut, ESPECIALIDAD, bloque, "WEB", true, AHORA);
        assertThat(r.statusCode()).as("creacion reserva en espera: " + r.asString()).isEqualTo(201);
        assertEsquema(r, "reserva.schema.json");
        assertThat(r.jsonPath().getString("estado")).isEqualTo("EN_ESPERA");
        return r.jsonPath().getLong("id");
    }

    private static Escenario armarEscenario() {
        ReservaCreada primera = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        return new Escenario(primera, crearEnEspera(primera.fechaHora()));
    }

    /**
     * Reserva autonoma (BORRADOR) cuyo dia deja al menos un dia habil posterior dentro de la
     * ventana del asignador (dias 2 a 55), para que bloquePosterior siempre tenga candidatos.
     */
    private static ReservaCreada reservaAutonoma() {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            ReservaCreada reserva = ClienteReservas.crearReservaOk(
                    PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
            if (!reserva.fechaHora().toLocalDate().isAfter(AHORA.toLocalDate().plusDays(50))) {
                return reserva;
            }
        }
        throw new AssertionError("Sin reserva con holgura tras " + MAX_INTENTOS + " intentos");
    }

    private static LocalDateTime bloqueDistinto(Set<LocalDate> diasUsados) {
        for (int i = 0; i < MAX_SORTEOS; i++) {
            LocalDateTime bloque = AsignadorBloques.siguiente(AHORA);
            if (!diasUsados.contains(bloque.toLocalDate())) {
                return bloque;
            }
        }
        throw new AssertionError("Sin bloque en otro dia");
    }

    private static LocalDateTime bloquePosterior(LocalDate dia) {
        for (int i = 0; i < MAX_SORTEOS; i++) {
            LocalDateTime bloque = AsignadorBloques.siguiente(AHORA);
            if (bloque.toLocalDate().isAfter(dia)) {
                return bloque;
            }
        }
        throw new AssertionError("Sin bloque en un dia posterior a " + dia);
    }

    /** La base persiste entre corridas: ante RES-CUPO se reintenta con otro bloque. */
    private static Modificacion modificarAOtroDia(long id, Supplier<LocalDateTime> bloques) {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            LocalDateTime nueva = bloques.get();
            Response respuesta = ClienteReservas.modificar(id, nueva, AHORA);
            if (respuesta.statusCode() != 409
                    || !"RES-CUPO".equals(respuesta.jsonPath().getString("codigo"))) {
                return new Modificacion(respuesta, nueva);
            }
        }
        throw new AssertionError("Sin bloque libre tras " + MAX_INTENTOS + " intentos");
    }

    /** Multa de porcentaje dado sobre el copago, redondeada al peso superior. */
    private static int multaEsperada(int copago, int porcentaje) {
        return (int) Math.ceil(copago * porcentaje / 100.0);
    }

    private static void assertReserva(Response r, String estado, int multa, int modificaciones) {
        assertEsquema(r, "reserva.schema.json");
        assertThat(r.jsonPath().getString("estado")).as("estado").isEqualTo(estado);
        assertThat(r.jsonPath().getInt("multa")).as("multa").isEqualTo(multa);
        assertThat(r.jsonPath().getInt("modificaciones")).as("modificaciones").isEqualTo(modificaciones);
    }

    private static void assertModificada(Modificacion m, String estado, int modificaciones) {
        assertThat(m.respuesta().statusCode()).as("estado HTTP: " + m.respuesta().asString()).isEqualTo(200);
        assertReserva(m.respuesta(), estado, 0, modificaciones);
        assertThat(LocalDateTime.parse(m.respuesta().jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(m.nuevaFechaHora());
    }

    private static void assertConsulta(long id, LocalDateTime reloj, String estado, int multa,
                                       int modificaciones) {
        assertReserva(ClienteReservas.obtener(id, reloj), estado, multa, modificaciones);
    }

    /** Anula la primera a 300 min (sin multa) y verifica que la segunda queda CONFIRMADA. */
    private static LocalDateTime anularPrimeraYVerificarPromocion(Escenario e) {
        LocalDateTime reloj = e.primera().fechaHora().minusMinutes(300);
        Response anulada = ClienteReservas.anular(e.primera().id(), reloj);
        assertThat(anulada.statusCode()).as("estado HTTP: " + anulada.asString()).isEqualTo(200);
        assertReserva(anulada, "ANULADA", 0, 0);
        assertConsulta(e.primera().id(), reloj, "ANULADA", 0, 0);
        assertConsulta(e.idSegunda(), reloj, "CONFIRMADA", 0, 0);
        return reloj;
    }

    // ------------------------------------------------------------- secuencias

    // T07 -> T11: la reserva promovida queda CONFIRMADA y luego su inasistencia cobra 50 % del copago.
    @Test
    @DisplayName("CP-36: promover desde la lista de espera y luego registrar inasistencia")
    void cp36_promoverYLuegoInasistencia() {
        Escenario e = armarEscenario();
        anularPrimeraYVerificarPromocion(e);
        int copago = ClienteReservas.obtener(e.idSegunda(), AHORA).jsonPath().getInt("copago");
        LocalDateTime reloj = e.primera().fechaHora().plusMinutes(30);

        Response respuesta = ClienteReservas.inasistencia(e.idSegunda(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        int multa = multaEsperada(copago, 50);
        assertReserva(respuesta, "NO_ASISTIDA", multa, 0);
        assertConsulta(e.idSegunda(), reloj, "NO_ASISTIDA", multa, 0);
    }

    // T07 -> T10: la reserva promovida se anula a 120 min y cobra 30 % del copago.
    @Test
    @DisplayName("CP-37: promover desde la lista de espera y luego anular la promovida")
    void cp37_promoverYLuegoAnular() {
        Escenario e = armarEscenario();
        anularPrimeraYVerificarPromocion(e);
        int copago = ClienteReservas.obtener(e.idSegunda(), AHORA).jsonPath().getInt("copago");
        LocalDateTime reloj = e.primera().fechaHora().minusMinutes(120);

        Response respuesta = ClienteReservas.anular(e.idSegunda(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        int multa = multaEsperada(copago, 30);
        assertReserva(respuesta, "ANULADA", multa, 0);
        assertConsulta(e.idSegunda(), reloj, "ANULADA", multa, 0);
    }

    // T07 -> T12: la reserva promovida se modifica a otro dia y cuenta una modificacion.
    @Test
    @DisplayName("CP-38: promover desde la lista de espera y luego modificar la promovida")
    void cp38_promoverYLuegoModificar() {
        Escenario e = armarEscenario();
        anularPrimeraYVerificarPromocion(e);
        Set<LocalDate> usados = Set.of(e.primera().fechaHora().toLocalDate());

        Modificacion m = modificarAOtroDia(e.idSegunda(), () -> bloqueDistinto(usados));

        assertModificada(m, "CONFIRMADA", 1);
        Response consulta = ClienteReservas.obtener(e.idSegunda(), AHORA);
        assertReserva(consulta, "CONFIRMADA", 0, 1);
        assertThat(LocalDateTime.parse(consulta.jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(m.nuevaFechaHora());
    }

    // T12 -> T10: la multa se mide contra la fecha nueva y el copago vigente tras modificar.
    @Test
    @DisplayName("CP-39: modificar una reserva CONFIRMADA y luego anularla")
    void cp39_modificarConfirmadaYLuegoAnular() {
        ReservaCreada reserva = reservaAutonoma();
        ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);
        LocalDate diaOriginal = reserva.fechaHora().toLocalDate();

        Modificacion m = modificarAOtroDia(reserva.id(), () -> bloquePosterior(diaOriginal));

        assertModificada(m, "CONFIRMADA", 1);
        int copago = m.respuesta().jsonPath().getInt("copago");
        LocalDateTime reloj = m.nuevaFechaHora().minusMinutes(120);
        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        int multa = multaEsperada(copago, 30);
        assertReserva(respuesta, "ANULADA", multa, 1);
        assertConsulta(reserva.id(), reloj, "ANULADA", multa, 1);
    }

    // T12 -> T11: inasistencia tras modificar cobra 50 % del copago recalculado y conserva el contador.
    @Test
    @DisplayName("CP-40: modificar una reserva CONFIRMADA y luego registrar inasistencia")
    void cp40_modificarConfirmadaYLuegoInasistencia() {
        ReservaCreada reserva = reservaAutonoma();
        ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);
        Set<LocalDate> usados = Set.of(reserva.fechaHora().toLocalDate());

        Modificacion m = modificarAOtroDia(reserva.id(), () -> bloqueDistinto(usados));

        assertModificada(m, "CONFIRMADA", 1);
        int copago = m.respuesta().jsonPath().getInt("copago");
        LocalDateTime reloj = m.nuevaFechaHora().plusMinutes(30);
        Response respuesta = ClienteReservas.inasistencia(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        int multa = multaEsperada(copago, 50);
        assertReserva(respuesta, "NO_ASISTIDA", multa, 1);
        assertConsulta(reserva.id(), reloj, "NO_ASISTIDA", multa, 1);
    }

    // T05 -> T04; oraculo (supuesto del equipo): anular un BORRADOR no genera multa. Falla por DEF-12 (multa 7500,
    // igual que CP-25); evidencia en el registro de corrida con INCLUDE_DISABLED.
    @Test
    @Disabled("DEF-12: anular un BORRADOR cobra multa por anticipacion (Reserva.anular no considera el estado de origen). Ver Bitacora de Defectos.")
    @DisplayName("CP-41: modificar una reserva en BORRADOR y luego anularla no genera multa")
    void cp41_modificarBorradorYLuegoAnular() {
        ReservaCreada reserva = reservaAutonoma();
        LocalDate diaOriginal = reserva.fechaHora().toLocalDate();

        Modificacion m = modificarAOtroDia(reserva.id(), () -> bloquePosterior(diaOriginal));

        assertModificada(m, "BORRADOR", 1);
        assertConsulta(reserva.id(), AHORA, "BORRADOR", 0, 1);
        LocalDateTime reloj = m.nuevaFechaHora().minusMinutes(120);
        Response respuesta = ClienteReservas.anular(reserva.id(), reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        assertReserva(respuesta, "ANULADA", 0, 1);
        assertConsulta(reserva.id(), reloj, "ANULADA", 0, 1);
    }

    // T12 -> T12: dos modificaciones a dias distintos; la segunda queda en el tope (RN-RES-09).
    @Test
    @DisplayName("CP-42: dos modificaciones de una reserva CONFIRMADA a dias distintos")
    void cp42_dosModificacionesConfirmada() {
        ReservaCreada reserva = reservaAutonoma();
        ClienteReservas.llevarA("CONFIRMADA", reserva.id(), AHORA);

        dosModificaciones(reserva, "CONFIRMADA");
    }

    // T05 -> T05: misma guarda de Reserva.mover en estado BORRADOR.
    @Test
    @DisplayName("CP-43: dos modificaciones de una reserva en BORRADOR a dias distintos")
    void cp43_dosModificacionesBorrador() {
        ReservaCreada reserva = reservaAutonoma();

        dosModificaciones(reserva, "BORRADOR");
    }

    private static void dosModificaciones(ReservaCreada reserva, String estado) {
        Set<LocalDate> usados = Set.of(reserva.fechaHora().toLocalDate());
        Modificacion primera = modificarAOtroDia(reserva.id(), () -> bloqueDistinto(usados));
        assertModificada(primera, estado, 1);
        assertConsulta(reserva.id(), AHORA, estado, 0, 1);

        Set<LocalDate> usados2 = Set.of(reserva.fechaHora().toLocalDate(),
                primera.nuevaFechaHora().toLocalDate());
        Modificacion segunda = modificarAOtroDia(reserva.id(), () -> bloqueDistinto(usados2));

        assertModificada(segunda, estado, 2);
        Response consulta = ClienteReservas.obtener(reserva.id(), AHORA);
        assertReserva(consulta, estado, 0, 2);
        assertThat(LocalDateTime.parse(consulta.jsonPath().getString("fechaHoraAtencion")))
                .isEqualTo(segunda.nuevaFechaHora());
    }

    // ----------------------------------------------------- efecto de la anulacion

    // T02 -> T08. Oraculo (supuestos del equipo): anular una reserva EN_ESPERA no libera cupo (no lo
    // ocupaba) y por lo tanto no promueve a otra; el bloque nunca tiene dos CONFIRMADA. Bloque con
    // una CONFIRMADA y dos EN_ESPERA: se anula una y la otra debe seguir EN_ESPERA. La multa no se
    // afirma aqui (DEF-12 la cubre CP-11). No agrega un par 1-switch: T02-T08 ya cuenta via CP-11.
    @Test
    @Disabled("DEF-14: promoverListaDeEspera corre tras anular una reserva EN_ESPERA y confirma a otra EN_ESPERA del bloque. Ver Bitacora de Defectos.")
    @DisplayName("CP-44: anular una reserva EN_ESPERA no promueve a otra EN_ESPERA del bloque")
    void cp44_anularEnEsperaNoPromueveOtraEnEspera() {
        ReservaCreada titular = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        ClienteReservas.llevarA("CONFIRMADA", titular.id(), AHORA);
        long espera1 = crearEnEspera(titular.fechaHora());
        long espera2 = crearEnEspera(titular.fechaHora());
        LocalDateTime reloj = titular.fechaHora().minusMinutes(300);

        Response respuesta = ClienteReservas.anular(espera2, reloj);

        assertThat(respuesta.statusCode()).as("estado HTTP: " + respuesta.asString()).isEqualTo(200);
        assertEsquema(respuesta, "reserva.schema.json");
        assertThat(respuesta.jsonPath().getString("estado")).isEqualTo("ANULADA");
        String estadoTitular = ClienteReservas.obtener(titular.id(), reloj).jsonPath().getString("estado");
        String estadoEspera1 = ClienteReservas.obtener(espera1, reloj).jsonPath().getString("estado");
        String estadoEspera2 = ClienteReservas.obtener(espera2, reloj).jsonPath().getString("estado");
        assertThat(estadoEspera2).isEqualTo("ANULADA");
        assertThat(estadoTitular).as("titular del bloque").isEqualTo("CONFIRMADA");
        assertThat(estadoEspera1).as("otra reserva EN_ESPERA del bloque").isEqualTo("EN_ESPERA");
        assertThat(java.util.stream.Stream.of(estadoTitular, estadoEspera1, estadoEspera2)
                .filter("CONFIRMADA"::equals).count()).as("CONFIRMADA en el bloque").isEqualTo(1);
    }
}
