package cl.losandes.mediturno.reservas.dominio;

/**
 * Resultado de tarificar una atención.
 *
 * @param arancelBruto arancel base más recargos, antes de bonificar
 * @param copago       lo que efectivamente paga el paciente
 */
public record Tarifa(long arancelBruto, long copago) {
}
