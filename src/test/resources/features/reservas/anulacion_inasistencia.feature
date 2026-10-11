# language: es

@reservas @anulacion
Característica: Anulación e inasistencia de una reserva

  Como paciente de MediTurno
  quiero conocer las consecuencias de anular o no asistir a una reserva
  para que se apliquen correctamente las multas y bloqueos definidos.

  @GH_RES_009 @RN_RES_05 @pairwise
  Esquema del escenario: Anular con más de cuatro horas de anticipación sin costo
    Dado que existe una reserva confirmada
    Y la atención tiene anticipación <anticipacion>
    Y el paciente tiene previsión <prevision>
    Cuando el paciente anula la reserva
    Entonces la reserva debe quedar anulada
    Y no debe aplicarse multa

    Ejemplos:
      | caso_origen | anticipacion                 | prevision |
      | PW-006      | MAYOR_240_MIN_MENOR_60D      | ISAPRE    |
      | PW-010      | MAYOR_240_MIN_MENOR_60D      | PARTICULAR |
      | PW-021      | MAYOR_240_MIN_MENOR_60D      | FONASA_A  |
      | PW-024      | MAYOR_240_MIN_MENOR_60D      | FONASA_BD |

  @GH_RES_010 @RN_RES_06 @limite
  Esquema del escenario: Aplicar multa del 30 por ciento al anular entre cuatro y una hora
    Dado que existe una reserva confirmada
    Y faltan exactamente <minutos> minutos para la atención
    Cuando el paciente anula la reserva
    Entonces la reserva debe quedar anulada
    Y debe aplicarse una multa del 30 por ciento del copago

    Ejemplos:
      | caso_origen | minutos |
      | VL-RES06-01 | 240     |
      | VL-RES06-02 | 239     |
      | VL-RES06-03 | 60      |

  @GH_RES_011 @RN_RES_07 @limite @pairwise
  Esquema del escenario: Aplicar multa del 50 por ciento al anular con menos de una hora
    Dado que existe una reserva confirmada
    Y faltan <minutos> minutos para la atención
    Cuando el paciente anula la reserva
    Entonces la reserva debe quedar anulada
    Y debe aplicarse una multa del 50 por ciento del copago

    Ejemplos:
      | caso_origen | minutos |
      | VL-RES07-01 | 59      |
      | PW-002      | 30      |
      | PW-025      | 30      |

  @GH_RES_012 @RN_RES_07 @decision
  Esquema del escenario: Aplicar multa del 50 por ciento por inasistencia
    Dado que existe una reserva confirmada
    Y el paciente <asistencia> a la atención
    Cuando se registra el resultado de la atención
    Entonces la reserva debe quedar en estado "NO_ASISTIDA"
    Y debe aplicarse una multa del 50 por ciento del copago

    Ejemplos:
      | caso_origen | asistencia   |
      | TD-RES07-01 | no se presenta |

  @GH_RES_013 @RN_RES_08 @pairwise @3wise
  Esquema del escenario: Bloquear la reserva en línea por tres inasistencias
    Dado que el paciente posee <historial> dentro de los últimos seis meses
    Y el bloqueo de 30 días continúa vigente
    Cuando intenta reservar mediante el canal <canal>
    Entonces la reserva debe ser rechazada por bloqueo de inasistencias

    Ejemplos:
      | caso_origen | historial                        | canal     |
      | PW-010      | 3_EN_6_MESES_BLOQUEO_VIGENTE    | WEB       |
      | PW-014      | 3_EN_6_MESES_BLOQUEO_VIGENTE    | APP_MOVIL |
      | PW-022      | 3_EN_6_MESES_BLOQUEO_VIGENTE    | APP_MOVIL |
      | PW-033      | 3_EN_6_MESES_BLOQUEO_VIGENTE    | APP_MOVIL |

  @GH_RES_014 @RN_RES_08 @pairwise @3wise
  Esquema del escenario: Permitir una reserva presencial o telefónica durante el bloqueo en línea
    Dado que el paciente posee <historial> dentro de los últimos seis meses
    Y el bloqueo de 30 días continúa vigente
    Cuando intenta reservar mediante el canal <canal>
    Entonces el bloqueo de reserva en línea no debe impedir la solicitud por ese canal

    Ejemplos:
      | caso_origen | historial                     | canal       |
      | PW-003      | 3_EN_6_MESES_BLOQUEO_VIGENTE | CALL_CENTER |
      | PW-007      | 3_EN_6_MESES_BLOQUEO_VIGENTE | PRESENCIAL  |

  @GH_RES_015 @RN_RES_08 @pairwise
  Esquema del escenario: Permitir nuevamente la reserva en línea al expirar el bloqueo
    Dado que el paciente posee <historial>
    Y el bloqueo de 30 días ya expiró
    Cuando intenta reservar mediante el canal <canal>
    Entonces el paciente debe poder continuar con la reserva

    Ejemplos:
      | caso_origen | historial                         | canal     |
      | PW-019      | 3_EN_6_MESES_BLOQUEO_EXPIRADO    | WEB       |
      | PW-028      | 3_EN_6_MESES_BLOQUEO_EXPIRADO    | APP_MOVIL |