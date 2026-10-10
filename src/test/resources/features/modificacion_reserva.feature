# language: es

@reservas @modificacion
Característica: Modificación de una reserva médica

  Como paciente de MediTurno
  quiero modificar una reserva existente
  para cambiar sus datos dentro del límite permitido.

  # RN-RES-09: máximo de dos modificaciones;
  # la tercera obliga a anular y crear una nueva.
  #
  # Origen de casos:
  # - partición de equivalencia
  # - valores límite del contador de modificaciones