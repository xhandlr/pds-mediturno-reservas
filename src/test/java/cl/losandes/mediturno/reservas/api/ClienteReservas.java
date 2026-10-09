package cl.losandes.mediturno.reservas.api;

import io.restassured.response.Response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Llamadas a la API de pacientes y reservas para armar escenarios. Todos los métodos de acción
 * reciben el reloj simulado a usar (ej. fechaAtencion menos N minutos para anular).
 */
public final class ClienteReservas {

    private static final Set<String> RUTS = ConcurrentHashMap.newKeySet();
    private static final int MAX_INTENTOS = 10;

    private ClienteReservas() {
    }

    /** Datos de un paciente de prueba. tramoFonasa puede ser null. */
    public record PerfilPaciente(String prevision, String tramoFonasa, boolean convenioEmpresa,
                                 LocalDate fechaNacimiento) {

        /** Edad 40 al reloj AHORA, sin convenio: copago == arancelBruto. */
        public static PerfilPaciente particular() {
            return new PerfilPaciente("PARTICULAR", null, false, LocalDate.of(1986, 5, 10));
        }

        public static PerfilPaciente fonasa(String tramo) {
            return new PerfilPaciente("FONASA", tramo, false, LocalDate.of(1986, 5, 10));
        }

        public static PerfilPaciente fonasaA() { return fonasa("A"); }
        public static PerfilPaciente fonasaB() { return fonasa("B"); }
        public static PerfilPaciente fonasaC() { return fonasa("C"); }
        public static PerfilPaciente fonasaD() { return fonasa("D"); }

        public static PerfilPaciente isapre() {
            return new PerfilPaciente("ISAPRE", null, false, LocalDate.of(1986, 5, 10));
        }

        public PerfilPaciente conConvenio() {
            return new PerfilPaciente(prevision, tramoFonasa, true, fechaNacimiento);
        }

        public PerfilPaciente nacidoEl(LocalDate fecha) {
            return new PerfilPaciente(prevision, tramoFonasa, convenioEmpresa, fecha);
        }
    }

    /** Resultado de una creación exitosa. */
    public record ReservaCreada(long id, String estado, String rut, String especialidad,
                                LocalDateTime fechaHora) {
    }

    // ------------------------------------------------------------- pacientes

    /** RUT único (en la JVM) de a lo más 10 caracteres. */
    public static String rutNuevo() {
        while (true) {
            int n = ThreadLocalRandom.current().nextInt(10_000_000, 99_999_999);
            String rut = n + "-" + (n % 10);
            if (RUTS.add(rut)) {
                return rut;
            }
        }
    }

    /** Crea un paciente nuevo (reintenta si el RUT ya existe en la base) y devuelve su RUT. */
    public static String crearPaciente(PerfilPaciente perfil) {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            String rut = rutNuevo();
            Response r = crearPacienteConRut(rut, perfil);
            if (r.statusCode() == 201) {
                return rut;
            }
            assertThat(r.statusCode()).as("creacion de paciente: " + r.asString()).isEqualTo(409);
        }
        throw new AssertionError("No se pudo crear un paciente con RUT único");
    }

    /** Crea un paciente con un RUT dado y devuelve la respuesta cruda. */
    public static Response crearPacienteConRut(String rut, PerfilPaciente perfil) {
        String cuerpo = """
                {"rut":"%s","nombre":"Paciente de Prueba API","fechaNacimiento":"%s",\
                "prevision":"%s","tramoFonasa":%s,"convenioEmpresa":%s}"""
                .formatted(rut, perfil.fechaNacimiento(), perfil.prevision(),
                        perfil.tramoFonasa() == null ? "null" : "\"" + perfil.tramoFonasa() + "\"",
                        perfil.convenioEmpresa());
        return BaseApiTest.spec(BaseApiTest.AHORA).body(cuerpo).post("/api/pacientes");
    }

    // -------------------------------------------------------------- reservas

    /** Creación cruda con fechaHora explícita; devuelve la respuesta sin validar. */
    public static Response crearReserva(String rut, String especialidad, LocalDateTime fechaHora,
                                        String canal, boolean aceptaListaEspera, LocalDateTime reloj) {
        String cuerpo = """
                {"rutPaciente":"%s","especialidad":"%s","fechaHoraAtencion":"%s",\
                "canal":"%s","aceptaListaEspera":%s}"""
                .formatted(rut, especialidad, fechaHora, canal, aceptaListaEspera);
        return BaseApiTest.spec(reloj).body(cuerpo).post("/api/reservas");
    }

    /**
     * Crea con un bloque nuevo (AsignadorBloques desde reloj); reintenta con otro bloque ante
     * RES-CUPO. Exige 201.
     */
    public static ReservaCreada crearReservaOk(String rut, String especialidad, String canal,
                                               boolean aceptaListaEspera, LocalDateTime reloj) {
        for (int i = 0; i < MAX_INTENTOS; i++) {
            LocalDateTime bloque = AsignadorBloques.siguiente(reloj);
            Response r = crearReserva(rut, especialidad, bloque, canal, aceptaListaEspera, reloj);
            if (r.statusCode() == 201) {
                return new ReservaCreada(r.jsonPath().getLong("id"), r.jsonPath().getString("estado"),
                        rut, especialidad, bloque);
            }
            if (!"RES-CUPO".equals(r.jsonPath().getString("codigo"))) {
                throw new AssertionError("Creación inesperada: " + r.statusCode() + " " + r.asString());
            }
        }
        throw new AssertionError("Sin cupo tras " + MAX_INTENTOS + " intentos");
    }

    /** Atajo: WEB, sin lista de espera, bloque nuevo. */
    public static ReservaCreada crearReservaOk(String rut, String especialidad, LocalDateTime reloj) {
        return crearReservaOk(rut, especialidad, "WEB", false, reloj);
    }

    /** Igual que crearReservaOk pero con paciente nuevo del perfil dado. */
    public static ReservaCreada crearReservaOk(PerfilPaciente perfil, String especialidad,
                                               LocalDateTime reloj) {
        return crearReservaOk(crearPaciente(perfil), especialidad, "WEB", false, reloj);
    }

    public static Response obtener(long id, LocalDateTime reloj) {
        return BaseApiTest.spec(reloj).get("/api/reservas/{id}", id);
    }

    public static Response modificar(long id, LocalDateTime nuevaFechaHora, LocalDateTime reloj) {
        return BaseApiTest.spec(reloj)
                .body("{\"fechaHoraAtencion\":\"" + nuevaFechaHora + "\"}")
                .patch("/api/reservas/{id}", id);
    }

    public static Response confirmar(long id, LocalDateTime reloj) {
        return accion(id, "confirmar", reloj);
    }

    public static Response iniciar(long id, LocalDateTime reloj) {
        return accion(id, "iniciar", reloj);
    }

    public static Response finalizar(long id, LocalDateTime reloj) {
        return accion(id, "finalizar", reloj);
    }

    public static Response anular(long id, LocalDateTime reloj) {
        return accion(id, "anular", reloj);
    }

    public static Response inasistencia(long id, LocalDateTime reloj) {
        return accion(id, "inasistencia", reloj);
    }

    private static Response accion(long id, String accion, LocalDateTime reloj) {
        return BaseApiTest.spec(reloj).post("/api/reservas/{id}/" + accion, id);
    }

    /**
     * Encadena transiciones exigiendo 200 en cada una, con el mismo reloj, hasta el estado
     * pedido: BORRADOR (nada), CONFIRMADA, EN_CURSO o ATENDIDA. Devuelve la última respuesta
     * (o null si estado es BORRADOR).
     */
    public static Response llevarA(String estado, long id, LocalDateTime reloj) {
        Response ultima = null;
        switch (estado) {
            case "BORRADOR" -> { }
            case "CONFIRMADA" -> ultima = exigirOk(confirmar(id, reloj));
            case "EN_CURSO" -> {
                exigirOk(confirmar(id, reloj));
                ultima = exigirOk(iniciar(id, reloj));
            }
            case "ATENDIDA" -> {
                exigirOk(confirmar(id, reloj));
                exigirOk(iniciar(id, reloj));
                ultima = exigirOk(finalizar(id, reloj));
            }
            default -> throw new IllegalArgumentException("Estado no soportado: " + estado);
        }
        return ultima;
    }

    private static Response exigirOk(Response r) {
        assertThat(r.statusCode()).as("transicion: " + r.asString()).isEqualTo(200);
        return r;
    }
}
