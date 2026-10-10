# language: es

@reservas @estados
Característica: Ciclo de vida de una reserva médica

  Como sistema de reservas
  quiero controlar las transiciones entre estados
  para preservar un ciclo de vida válido de cada reserva.

  @GH_RES_018 @RN_RES_10 @estado @0switch
  Esquema del escenario: Confirmar una reserva en borrador
    Dado que existe una reserva en estado "<origen>"
    Cuando la reserva es confirmada
    Entonces debe quedar en estado "<destino>"

    Ejemplos:
      | caso_origen | origen   | destino    |
      | EST-10-01   | BORRADOR | CONFIRMADA |

  @GH_RES_019 @RN_RES_10 @estado @0switch
  Esquema del escenario: Iniciar una reserva confirmada
    Dado que existe una reserva en estado "<origen>"
    Cuando comienza la atención
    Entonces debe quedar en estado "<destino>"

    Ejemplos:
      | caso_origen | origen     | destino  |
      | EST-10-02   | CONFIRMADA | EN_CURSO |

  @GH_RES_020 @RN_RES_10 @estado @0switch
  Esquema del escenario: Finalizar una atención en curso
    Dado que existe una reserva en estado "<origen>"
    Cuando finaliza la atención
    Entonces debe quedar en estado "<destino>"

    Ejemplos:
      | caso_origen | origen   | destino  |
      | EST-10-03   | EN_CURSO | ATENDIDA |

  @GH_RES_021 @RN_RES_10 @estado @pairwise
  Esquema del escenario: Dejar una reserva en espera cuando el bloque está ocupado
    Dado que el bloque solicitado está <cupo>
    Y el paciente acepta incorporarse a la lista de espera
    Cuando intenta crear la reserva
    Entonces la reserva debe quedar en estado "EN_ESPERA"

    Ejemplos:
      | caso_origen | cupo                |
      | PW-004      | OCUPADO_CON_ESPERA  |
      | PW-006      | OCUPADO_CON_ESPERA  |
      | PW-009      | OCUPADO_CON_ESPERA  |

  @GH_RES_022 @RN_RES_10 @estado @1switch
  Esquema del escenario: Promover una reserva en espera cuando se libera un cupo
    Dado que existe una reserva en estado "EN_ESPERA"
    Y existe otra reserva confirmada en el mismo bloque
    Cuando la reserva confirmada es anulada y libera el cupo
    Entonces la primera reserva en espera debe quedar en estado "CONFIRMADA"

    Ejemplos:
      | caso_origen |
      | EST-10-04   |