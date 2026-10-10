# language: es

@reservas @estados
Característica: Ciclo de vida de una reserva médica

  Como sistema de reservas
  quiero controlar las transiciones entre estados
  para preservar un ciclo de vida válido de la reserva.

  # RN-RES-10
  #
  # Estados:
  # - BORRADOR
  # - CONFIRMADA
  # - EN_CURSO
  # - ATENDIDA
  # - ANULADA
  # - NO_ASISTIDA
  # - EN_ESPERA
  #
  # Origen de casos:
  # - máquina de estados según especificación
  # - tabla de transiciones
  # - cobertura 0-switch
  # - caminos críticos 1-switch
  #
  # Las transiciones no especificadas por RN-RES-10 se documentarán
  # como hallazgos y no se asumirán arbitrariamente como válidas.