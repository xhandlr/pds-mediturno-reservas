# language: es

@reservas @creacion
Característica: Reserva de una hora médica

  Como paciente de MediTurno
  quiero reservar una hora médica
  para acceder a una atención respetando las reglas de disponibilidad
  y las restricciones de reserva.

  @GH_RES_001 @RN_RES_01 @limite
  Esquema del escenario: Reservar en un bloque válido de lunes a viernes
    Dado que el paciente solicita una reserva un <dia>
    Y la hora solicitada es <hora>
    Cuando intenta crear la reserva
    Entonces la reserva debe ser aceptada como un bloque horario válido

    Ejemplos:
      | caso_origen | dia       | hora  |
      | VL-RES01-01 | lunes     | 08:00 |
      | VL-RES01-02 | miércoles | 12:20 |
      | VL-RES01-03 | viernes   | 18:00 |

  @GH_RES_002 @RN_RES_01 @limite
  Esquema del escenario: Reservar en un bloque válido de sábado
    Dado que el paciente solicita una reserva un sábado
    Y la hora solicitada es <hora>
    Cuando intenta crear la reserva
    Entonces la reserva debe ser aceptada como un bloque horario válido

    Ejemplos:
      | caso_origen | hora  |
      | VL-RES01-04 | 09:00 |
      | VL-RES01-05 | 11:20 |
      | VL-RES01-06 | 14:00 |

  @GH_RES_003 @RN_RES_01 @limite
  Esquema del escenario: Rechazar una reserva fuera de los bloques permitidos
    Dado que el paciente solicita una reserva un <dia>
    Y la hora solicitada es <hora>
    Cuando intenta crear la reserva
    Entonces la reserva debe ser rechazada por estar fuera de un bloque permitido

    Ejemplos:
      | caso_origen | dia     | hora  |
      | VL-RES01-07 | lunes   | 07:40 |
      | VL-RES01-08 | viernes | 18:20 |
      | VL-RES01-09 | sábado  | 08:40 |
      | VL-RES01-10 | sábado  | 14:20 |
      | VL-RES01-11 | domingo | 10:00 |

  @GH_RES_004 @RN_RES_02 @pairwise
  Esquema del escenario: Rechazar una reserva que no se encuentra en el futuro
    Dado un paciente con previsión <prevision>
    Y que solicita una atención con anticipación <anticipacion>
    Y utiliza el canal <canal>
    Cuando intenta crear la reserva
    Entonces la reserva debe ser rechazada por no encontrarse en el futuro

    Ejemplos:
      | caso_origen | prevision | anticipacion    | canal       |
      | PW-001      | FONASA_A  | PASADO_O_AHORA | WEB         |
      | PW-016      | FONASA_BD | PASADO_O_AHORA | CALL_CENTER |
      | PW-031      | ISAPRE    | PASADO_O_AHORA | PRESENCIAL  |
      | PW-042      | ISAPRE    | PASADO_O_AHORA | APP_MOVIL   |

  @GH_RES_005 @RN_RES_02 @pairwise @limite
  Esquema del escenario: Aceptar una reserva dentro de la ventana máxima permitida
    Dado un paciente con previsión <prevision>
    Y que solicita una atención con anticipación <anticipacion>
    Y el bloque solicitado se encuentra <cupo>
    Cuando intenta crear la reserva
    Entonces la anticipación debe ser considerada válida

    Ejemplos:
      | caso_origen | prevision | anticipacion                | cupo        |
      | PW-013      | ISAPRE    | 60_DIAS                     | DISPONIBLE  |
      | PW-021      | FONASA_A  | MAYOR_240_MIN_MENOR_60D     | DISPONIBLE  |
      | PW-024      | FONASA_BD | MAYOR_240_MIN_MENOR_60D     | DISPONIBLE  |
      | PW-035      | ISAPRE    | 60_DIAS                     | OCUPADO_CON_ESPERA |

  @GH_RES_006 @RN_RES_02 @pairwise @limite
  Esquema del escenario: Rechazar una reserva con más de 60 días de anticipación
    Dado un paciente con previsión <prevision>
    Y que solicita una atención con anticipación <anticipacion>
    Y utiliza el canal <canal>
    Cuando intenta crear la reserva
    Entonces la reserva debe ser rechazada por superar los 60 días permitidos

    Ejemplos:
      | caso_origen | prevision | anticipacion  | canal       |
      | PW-008      | FONASA_A  | MAYOR_60_DIAS | CALL_CENTER |
      | PW-009      | FONASA_BD | MAYOR_60_DIAS | WEB         |
      | PW-018      | ISAPRE    | MAYOR_60_DIAS | PRESENCIAL  |
      | PW-030      | ISAPRE    | MAYOR_60_DIAS | APP_MOVIL   |

  @GH_RES_007 @RN_RES_03 @limite
  Esquema del escenario: Impedir superar el máximo de reservas activas
    Dado que el paciente ya posee <reservas_activas> reservas activas
    Cuando intenta crear una nueva reserva
    Entonces la nueva reserva debe ser <resultado>

    Ejemplos:
      | caso_origen | reservas_activas | resultado |
      | PE-RES03-01 | 2                | aceptada  |
      | PE-RES03-02 | 3                | rechazada |

  @GH_RES_008 @RN_RES_04 @limite
  Esquema del escenario: Impedir dos reservas de la misma especialidad en el mismo día
    Dado que el paciente posee una reserva de <especialidad> el día solicitado
    Y solicita otra reserva de <especialidad> para el mismo día a una hora diferente
    Cuando intenta crear la segunda reserva
    Entonces la nueva reserva debe ser rechazada por duplicidad de especialidad

    Ejemplos:
      | caso_origen | especialidad     |
      | PE-RES04-01 | MEDICINA_GENERAL |
      | PE-RES04-02 | PEDIATRIA        |
      | PE-RES04-03 | CARDIOLOGIA      |