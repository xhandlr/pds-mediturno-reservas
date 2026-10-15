package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

// Transiciones inválidas T16..T45 (CP-29..CP-35): un método por estado de origen. Los eventos de
// estado deben dar 409 RES-10-TRANSICION; modificar, 409 RES-09-ESTADO. La especificación no
// describe estas transiciones; se asume que no están permitidas.
class TransicionesInvalidasApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";
    private static final int MAX_INTENTOS = 10;

    /** Una transición inválida: ids del caso y de la transición, estado de origen y evento. */
    record Caso(String cp, String t, String origen, String evento) {
        @Override
        public String toString() {
            return cp + " " + t + ": " + evento + " desde " + origen + " se rechaza";
        }
    }

    /** Reserva ya llevada al estado de origen, con el reloj a usar para el evento inválido. */
    private record Preparada(long id, LocalDateTime fechaHora, LocalDateTime reloj) { }

    /** Foto de lo que no debe cambiar tras el rechazo. */
    private record Foto(String estado, int multa, int modificaciones) { }

    // ------------------------------------------------------------------ casos

    static Stream<Caso> cp29Borrador() {
        return Stream.of(
                new Caso("CP-29", "T16", "BORRADOR", "iniciar"),
                new Caso("CP-29", "T17", "BORRADOR", "finalizar"),
                new Caso("CP-29", "T18", "BORRADOR", "inasistencia"));
    }

    static Stream<Caso> cp30EnEspera() {
        return Stream.of(
                new Caso("CP-30", "T19", "EN_ESPERA", "iniciar"),
                new Caso("CP-30", "T20", "EN_ESPERA", "finalizar"),
                new Caso("CP-30", "T21", "EN_ESPERA", "inasistencia"),
                new Caso("CP-30", "T22", "EN_ESPERA", "modificar"));
    }

    static Stream<Caso> cp31Confirmada() {
        return Stream.of(
                new Caso("CP-31", "T23", "CONFIRMADA", "confirmar"),
                new Caso("CP-31", "T24", "CONFIRMADA", "finalizar"));
    }

    static Stream<Caso> cp32EnCurso() {
        return Stream.of(
                new Caso("CP-32", "T25", "EN_CURSO", "confirmar"),
                new Caso("CP-32", "T26", "EN_CURSO", "iniciar"),
                new Caso("CP-32", "T27", "EN_CURSO", "inasistencia"),
                new Caso("CP-32", "T28", "EN_CURSO", "modificar"));
    }

    static Stream<Caso> cp33Atendida() {
        return Stream.of(
                new Caso("CP-33", "T29", "ATENDIDA", "confirmar"),
                new Caso("CP-33", "T30", "ATENDIDA", "iniciar"),
                new Caso("CP-33", "T31", "ATENDIDA", "finalizar"),
                new Caso("CP-33", "T32", "ATENDIDA", "inasistencia"),
                new Caso("CP-33", "T33", "ATENDIDA", "modificar"));
    }

    static Stream<Caso> cp34Anulada() {
        return Stream.of(
                new Caso("CP-34", "T34", "ANULADA", "confirmar"),
                new Caso("CP-34", "T35", "ANULADA", "iniciar"),
                new Caso("CP-34", "T36", "ANULADA", "finalizar"),
                new Caso("CP-34", "T37", "ANULADA", "anular"),
                new Caso("CP-34", "T38", "ANULADA", "inasistencia"),
                new Caso("CP-34", "T39", "ANULADA", "modificar"));
    }

    static Stream<Caso> cp35NoAsistida() {
        return Stream.of(
                new Caso("CP-35", "T40", "NO_ASISTIDA", "confirmar"),
                new Caso("CP-35", "T41", "NO_ASISTIDA", "iniciar"),
                new Caso("CP-35", "T42", "NO_ASISTIDA", "finalizar"),
                new Caso("CP-35", "T43", "NO_ASISTIDA", "anular"),
                new Caso("CP-35", "T44", "NO_ASISTIDA", "inasistencia"),
                new Caso("CP-35", "T45", "NO_ASISTIDA", "modificar"));
    }

    // ---------------------------------------------------------------- pruebas

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp29Borrador")
    @DisplayName("CP-29: eventos inválidos desde BORRADOR")
    void cp29_eventosInvalidosDesdeBorrador(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp30EnEspera")
    @DisplayName("CP-30: eventos inválidos desde EN_ESPERA")
    void cp30_eventosInvalidosDesdeEnEspera(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp31Confirmada")
    @DisplayName("CP-31: eventos inválidos desde CONFIRMADA")
    void cp31_eventosInvalidosDesdeConfirmada(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp32EnCurso")
    @DisplayName("CP-32: eventos inválidos desde EN_CURSO")
    void cp32_eventosInvalidosDesdeEnCurso(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp33Atendida")
    @DisplayName("CP-33: eventos inválidos desde ATENDIDA")
    void cp33_eventosInvalidosDesdeAtendida(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp34Anulada")
    @DisplayName("CP-34: eventos inválidos desde ANULADA")
    void cp34_eventosInvalidosDesdeAnulada(Caso caso) {
        verificarRechazo(caso);
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("cp35NoAsistida")
    @DisplayName("CP-35: eventos inválidos desde NO_ASISTIDA")
    void cp35_eventosInvalidosDesdeNoAsistida(Caso caso) {
        verificarRechazo(caso);
    }

    // ---------------------------------------------------------------- soporte

    private static void verificarRechazo(Caso caso) {
        Preparada p = prepararEstado(caso.origen());
        Foto antes = foto(p.id(), p.reloj());
        assertThat(antes.estado()).as("estado de origen preparado").isEqualTo(caso.origen());

        Response respuesta;
        String codigo;
        if ("modificar".equals(caso.evento())) {
            respuesta = modificarABloqueLibre(p);
            codigo = "RES-09-ESTADO";
        } else {
            respuesta = ejecutar(caso.evento(), p.id(), p.reloj());
            codigo = "RES-10-TRANSICION";
        }

        assertError(respuesta, 409, codigo);
        assertEsquemaError(respuesta);
        // Estado, multa y modificaciones intactos (sin segunda multa en ANULADA / NO_ASISTIDA).
        assertThat(foto(p.id(), p.reloj())).isEqualTo(antes);
    }

    private static Response ejecutar(String evento, long id, LocalDateTime reloj) {
        return switch (evento) {
            case "confirmar" -> ClienteReservas.confirmar(id, reloj);
            case "iniciar" -> ClienteReservas.iniciar(id, reloj);
            case "finalizar" -> ClienteReservas.finalizar(id, reloj);
            case "anular" -> ClienteReservas.anular(id, reloj);
            case "inasistencia" -> ClienteReservas.inasistencia(id, reloj);
            default -> throw new IllegalArgumentException("Evento no soportado: " + evento);
        };
    }

    /**
     * Modificar a un bloque nuevo y libre, posterior al reloj del evento (la validación de fecha
     * precede a la de estado); reintenta con otro bloque ante RES-CUPO.
     */
    private static Response modificarABloqueLibre(Preparada p) {
        Response r = null;
        for (int i = 0; i < MAX_INTENTOS; i++) {
            LocalDateTime nuevo = AsignadorBloques.siguiente(p.reloj());
            r = ClienteReservas.modificar(p.id(), nuevo, p.reloj());
            if (!"RES-CUPO".equals(r.jsonPath().getString("codigo"))) {
                return r;
            }
        }
        return r;
    }

    private static Foto foto(long id, LocalDateTime reloj) {
        Response r = ClienteReservas.obtener(id, reloj);
        assertThat(r.statusCode()).as("consulta: " + r.asString()).isEqualTo(200);
        assertEsquema(r, "reserva.schema.json");
        return new Foto(r.jsonPath().getString("estado"), r.jsonPath().getInt("multa"),
                r.jsonPath().getInt("modificaciones"));
    }

    /** Lleva una reserva nueva (paciente PARTICULAR) al estado de origen pedido. */
    private static Preparada prepararEstado(String origen) {
        if ("EN_ESPERA".equals(origen)) {
            return prepararEnEspera();
        }
        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        LocalDateTime atencion = reserva.fechaHora();
        // 120 min antes: si hay multa, es distinta de cero y una segunda multa sería visible.
        LocalDateTime reloj = atencion.minusMinutes(120);
        switch (origen) {
            case "BORRADOR" -> { }
            case "CONFIRMADA", "EN_CURSO", "ATENDIDA" ->
                    ClienteReservas.llevarA(origen, reserva.id(), reloj);
            case "ANULADA" -> {
                ClienteReservas.llevarA("CONFIRMADA", reserva.id(), reloj);
                assertThat(ClienteReservas.anular(reserva.id(), reloj).statusCode()).isEqualTo(200);
            }
            case "NO_ASISTIDA" -> {
                ClienteReservas.llevarA("CONFIRMADA", reserva.id(), reloj);
                assertThat(ClienteReservas.inasistencia(reserva.id(), reloj).statusCode())
                        .isEqualTo(200);
            }
            default -> throw new IllegalArgumentException("Origen no soportado: " + origen);
        }
        return new Preparada(reserva.id(), atencion, reloj);
    }

    // Copia local del patrón armarEscenario de ListaDeEsperaApiTest: la 2.ª reserva del mismo
    // bloque, con aceptaListaEspera, queda EN_ESPERA.
    private static Preparada prepararEnEspera() {
        ReservaCreada primera = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(), ESPECIALIDAD, AHORA);
        String rutSegundo = ClienteReservas.crearPaciente(PerfilPaciente.particular());
        Response segunda = ClienteReservas.crearReserva(rutSegundo, ESPECIALIDAD,
                primera.fechaHora(), "WEB", true, AHORA);
        assertThat(segunda.statusCode()).as("creacion 2a reserva: " + segunda.asString()).isEqualTo(201);
        assertEsquema(segunda, "reserva.schema.json");
        assertThat(segunda.jsonPath().getString("estado")).isEqualTo("EN_ESPERA");
        return new Preparada(segunda.jsonPath().getLong("id"), primera.fechaHora(),
                primera.fechaHora().minusMinutes(120));
    }
}
