# language: es

@reservas @modificacion
Característica: Modificación de una reserva médica

  Como paciente de MediTurno
  quiero modificar una reserva existente
  respetando el número máximo de modificaciones permitido.

  @GH_RES_016 @RN_RES_09 @limite
  Esquema del escenario: Permitir hasta dos modificaciones de una reserva
    Dado que existe una reserva válida
    Y la reserva posee <modificaciones_previas> modificaciones previas
    Cuando el paciente solicita cambiar la hora de atención
    Entonces la modificación debe ser aceptada
    Y la reserva debe continuar vigente

    Ejemplos:
      | caso_origen | modificaciones_previas |
      | VL-RES09-01 | 0                      |
      | VL-RES09-02 | 1                      |

  @GH_RES_017 @RN_RES_09 @limite
  Esquema del escenario: Obligar a crear una nueva reserva en la tercera modificación
    Dado que existe una reserva válida
    Y la reserva posee <modificaciones_previas> modificaciones previas
    Cuando el paciente intenta modificarla nuevamente
    Entonces la reserva existente debe ser anulada
    Y debe requerirse la creación de una nueva reserva

    Ejemplos:
      | caso_origen | modificaciones_previas |
      | VL-RES09-03 | 2                      |