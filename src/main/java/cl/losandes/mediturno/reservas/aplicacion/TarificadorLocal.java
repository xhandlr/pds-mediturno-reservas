package cl.losandes.mediturno.reservas.aplicacion;
import cl.losandes.mediturno.reservas.dominio.*;

import java.time.LocalDateTime;

/**
 * Implementación local de las reglas RN-TAR-01 a RN-TAR-10.
 *
 * <p>La Entrega 2 no evalúa tarificación: este cálculo se entrega íntegro y correcto
 * para que el copago y las multas sean predecibles a partir del enunciado.</p>
 */
public class TarificadorLocal implements TarificadorPort {

    private static final long TOPE_ISAPRE = 30_000L;
    private static final int EDAD_ADULTO_MAYOR = 65;

    @Override
    public Tarifa tarificar(Paciente paciente, Especialidad especialidad,
                            LocalDateTime fechaHoraAtencion, LocalDateTime ahora) {
        long arancelBase = especialidad.arancelBase();

        // RN-TAR-05: atención el mismo día en que se solicita.
        boolean urgencia = fechaHoraAtencion.toLocalDate().equals(ahora.toLocalDate());
        // RN-TAR-06: después de las 18:00 o sábado.
        boolean horarioExtendido = fechaHoraAtencion.getHour() >= 18
                || fechaHoraAtencion.getDayOfWeek() == java.time.DayOfWeek.SATURDAY;

        long recargoUrgencia = urgencia ? arancelBase * 20 / 100 : 0L;
        long recargoHorarioExtendido = horarioExtendido ? arancelBase * 15 / 100 : 0L;
        long arancelBruto = arancelBase + recargoUrgencia + recargoHorarioExtendido;

        long bonificacion = switch (paciente.prevision()) {
            case FONASA -> arancelBruto * paciente.tramoFonasa().porcentajeBonificacion() / 100;
            case ISAPRE -> arancelBruto * 60 / 100;
            case PARTICULAR -> 0L;
        };

        long copago = arancelBruto - bonificacion;
        if (paciente.prevision() == TipoPrevision.ISAPRE && copago > TOPE_ISAPRE) {
            copago = TOPE_ISAPRE;
        }

        int porcentajeDescuento = 0;
        if (paciente.edadA(fechaHoraAtencion.toLocalDate()) >= EDAD_ADULTO_MAYOR) {
            porcentajeDescuento = 10;
        } else if (paciente.convenioEmpresa()) {
            porcentajeDescuento = 15;
        }
        copago -= copago * porcentajeDescuento / 100;
        if (copago < 0) {
            copago = 0;
        }
        return new Tarifa(arancelBruto, copago);
    }
}
