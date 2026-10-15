# Charter SBTM 1 (CH-01)

Tester: Nicole Rodríguez
Fecha: 2026-10-07 21:00
Duración: 60 min

## Objetivo
Explorar la modificación de reservas (PATCH) y la promoción desde la lista de
espera, con la API y la UI y reloj simulado fijo, para descubrir validaciones
que dejan de aplicarse en esos flujos.

## Alcance
- Dentro: PATCH de fecha/hora, lista de espera y su efecto en otras reservas.
  Reglas: RN-RES-01, 02, 03, 04, 08, 09 y 10.
- Fuera: cálculo de multas (DEF-06) y accesibilidad.

## Riesgos
Si PATCH o la promoción saltan validaciones, quedan reservas inválidas,
bloques sobrecargados o pacientes bloqueados que igual consiguen hora.
Puntos a investigar:
- PATCH a bloque fuera de calendario, pasado o >60 días (RN-RES-01, 02).
- PATCH que deja dos reservas de la misma especialidad el mismo día (RN-RES-04).
- PATCH a un bloque ocupado, o en estado EN_ESPERA, ANULADA o ATENDIDA
  (RN-RES-09, 10).
- Promoción desde la espera con tope de 3 o paciente bloqueado (RN-RES-03, 08).