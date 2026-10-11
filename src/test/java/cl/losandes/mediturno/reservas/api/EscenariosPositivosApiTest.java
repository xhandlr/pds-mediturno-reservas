package cl.losandes.mediturno.reservas.api;

import cl.losandes.mediturno.reservas.api.ClienteReservas.PerfilPaciente;
import cl.losandes.mediturno.reservas.api.ClienteReservas.ReservaCreada;
import io.restassured.response.Response;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class EscenariosPositivosApiTest extends BaseApiTest {

    private static final String ESPECIALIDAD = "MEDICINA_GENERAL";

    @Test
    @DisplayName("CP-16: crear y consultar una reserva válida")
    void cp16_crearYConsultarReservaValida() {

        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(),
                ESPECIALIDAD,
                AHORA
        );

        assertThat(reserva.estado()).isEqualTo("BORRADOR");

        Response consulta = ClienteReservas.obtener(
                reserva.id(),
                AHORA
        );

        assertThat(consulta.statusCode())
                .as("GET /api/reservas/{id}")
                .isEqualTo(200);

        assertEsquema(consulta, "reserva.schema.json");

        assertThat(consulta.jsonPath().getLong("id"))
                .isEqualTo(reserva.id());

        assertThat(consulta.jsonPath().getString("rutPaciente"))
                .isEqualTo(reserva.rut());

        assertThat(consulta.jsonPath().getString("especialidad"))
                .isEqualTo(ESPECIALIDAD);

        assertThat(consulta.jsonPath().getString("estado"))
                .isEqualTo("BORRADOR");
    }


    @Test
    @DisplayName("CP-17: listar reservas de un paciente")
    void cp17_listarReservasPorPaciente() {

        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(),
                ESPECIALIDAD,
                AHORA
        );

        Response respuesta = spec(AHORA)
                .queryParam("rut", reserva.rut())
                .get("/api/reservas");

        assertThat(respuesta.statusCode())
                .as("GET /api/reservas?rut=...")
                .isEqualTo(200);
        assertEsquema(
                respuesta,
                "lista-reservas.schema.json"
        );
        List<Number> ids = respuesta.jsonPath().getList("id");

        assertThat(ids)
                .anyMatch(id -> id.longValue() == reserva.id());
    }


    @Test
    @DisplayName("CP-18: ciclo completo BORRADOR a ATENDIDA")
    void cp18_cicloCompletoReserva() {

        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(),
                ESPECIALIDAD,
                AHORA
        );

        assertThat(reserva.estado())
                .isEqualTo("BORRADOR");

        Response confirmada = ClienteReservas.confirmar(
                reserva.id(),
                AHORA
        );

        assertThat(confirmada.statusCode()).isEqualTo(200);
        assertEsquema(confirmada, "reserva.schema.json");
        assertThat(confirmada.jsonPath().getString("estado"))
                .isEqualTo("CONFIRMADA");

        /*
         * El inicio se ejecuta usando como reloj simulado
         * la propia fecha de atención.
         */
        Response enCurso = ClienteReservas.iniciar(
                reserva.id(),
                reserva.fechaHora()
        );

        assertThat(enCurso.statusCode()).isEqualTo(200);
        assertThat(enCurso.jsonPath().getString("estado"))
                .isEqualTo("EN_CURSO");

        Response atendida = ClienteReservas.finalizar(
                reserva.id(),
                reserva.fechaHora().plusMinutes(20)
        );

        assertThat(atendida.statusCode()).isEqualTo(200);
        assertThat(atendida.jsonPath().getString("estado"))
                .isEqualTo("ATENDIDA");

        Response consulta = ClienteReservas.obtener(
                reserva.id(),
                reserva.fechaHora().plusMinutes(20)
        );

        assertThat(consulta.statusCode()).isEqualTo(200);
        assertThat(consulta.jsonPath().getString("estado"))
                .isEqualTo("ATENDIDA");
    }


    @Test
    @DisplayName("CP-19: anular con más de cuatro horas no genera multa")
    void cp19_anulacionMayorCuatroHorasSinMulta() {

        ReservaCreada reserva = ClienteReservas.crearReservaOk(
                PerfilPaciente.particular(),
                ESPECIALIDAD,
                AHORA
        );

        Response confirmacion = ClienteReservas.confirmar(
                reserva.id(),
                AHORA
        );

        assertThat(confirmacion.statusCode())
                .isEqualTo(200);

        /*
         * Cinco horas antes de la atención.
         * RN-RES-05 exige anulación sin costo.
         */
        LocalDateTime relojAnulacion =
                reserva.fechaHora().minusMinutes(300);

        Response anulada = ClienteReservas.anular(
                reserva.id(),
                relojAnulacion
        );

        assertThat(anulada.statusCode())
                .isEqualTo(200);
        assertEsquema(anulada, "reserva.schema.json");

        assertThat(anulada.jsonPath().getString("estado"))
                .isEqualTo("ANULADA");

        assertThat(anulada.jsonPath().getInt("multa"))
                .isZero();

        Response consulta = ClienteReservas.obtener(
                reserva.id(),
                relojAnulacion
        );
        assertEsquema(consulta, "reserva.schema.json");

        assertThat(consulta.jsonPath().getString("estado"))
                .isEqualTo("ANULADA");

        assertThat(consulta.jsonPath().getInt("multa"))
                .isZero();
    }
}
