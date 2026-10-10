# language: es

@reservas @anulacion
Característica: Anulación e inasistencia de una reserva

  Como paciente de MediTurno
  quiero conocer las consecuencias de anular o no asistir a una reserva
  para que se apliquen correctamente las reglas de multa y bloqueo.

  # RN-RES-05: anulación con más de 4 horas
  # RN-RES-06: anulación entre 4 horas y 1 hora
  # RN-RES-07: menos de 1 hora o inasistencia
  # RN-RES-08: bloqueo por inasistencias
  #
  # Origen de casos:
  # - tabla de decisión
  # - valores límite 240, 239, 60 y 59 minutos
  # - conjunto pairwise
  # - cobertura 3-wise de alto riesgo