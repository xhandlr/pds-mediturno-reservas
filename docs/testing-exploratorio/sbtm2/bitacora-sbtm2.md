# Bitácora SBTM — Sesión 2

## Identificación

- Sesión: SBTM-2
- Tester: Camilo Ñanco
- Duración objetivo: 60 minutos
- Charter: Explorar el comportamiento de la anulación de reservas y de las inasistencias alrededor de sus límites temporales, buscando inconsistencias en el cálculo de multas, el bloqueo de reservas en línea y la aplicación de RN-RES-05 a RN-RES-08.
- Medio principal: API REST
- Inicio: 18:15
- Fin:19:15

## Registro cronológico

| Hora | Tipo | Actividad | Esperado | Observado | Resultado / nota |
|---|---|---|---|---|---|
| 18:24 | Setup | Verificación del servicio ms-reservas y herramientas para la exploración | API disponible | HTTP 200; jq disponible | Ambiente preparado |
| 18:34 | Ejecución | Anulación con 4 h 01 min de anticipación para establecer un caso control de RN-RES-05 | Estado ANULADA y multa $0 | Estado ANULADA y multa $0 | Comportamiento conforme |
| 18:40 | Ejecución | Explorar precisión inmediatamente superior a 4 horas usando 4 h 00 min 01 s | Verificar cómo trata el sistema una diferencia con segundos | HTTP 200, estado ANULADA, multa $7.500 | Observación: el sistema aplica granularidad de minutos |
| 18:44 | Ejecución | Verificar la base usada para calcular la multa con paciente FONASA B y anulación a 120 min | RN-RES-06: 30 % del copago. Copago $2.500 → multa $750 | HTTP 200, estado ANULADA, arancel $25.000, copago $2.500, multa $7.500 | Falla. La multa se calcula sobre el arancel bruto en vez del copago. Hallazgo ya cubierto por CP-04 / DEF-06 |
| 18:55 | Ejecución | Explorar la ventana móvil de 6 meses usando una inasistencia antigua (02/03) y dos recientes (01/09 y 01/10), evaluando el 19/10 | Solo 2 inasistencias están dentro de los últimos 6 meses, por lo que una nueva reserva WEB debe permitirse | HTTP 409, código RES-08-BLOQUEO; el sistema bloquea la reserva WEB | Falla. Posible defecto en el cálculo de la ventana móvil de RN-RES-08; iniciar investigación |
| 18:57 | Diseño/Ejecución | Comprobar si el bloqueo por inasistencias afecta también al canal PRESENCIAL | Al tratarse de un bloqueo de reserva en línea, PRESENCIAL debe seguir permitido | HTTP 201, reserva creada en BORRADOR mediante PRESENCIAL | Comportamiento conforme; el problema observado se concentra en el cálculo de la ventana móvil |
|19:00 | Diseño/Ejecución | Preparar historial de 3 inasistencias dentro de seis meses para explorar la duración del bloqueo | La tercera queda NO_ASISTIDA y activa el bloqueo online | HTTP 200, estado NO_ASISTIDA, multa $16.000 | Datos de prueba preparados correctamente |
| 19:00 | Diseño/Ejecución | Intentar reserva WEB mientras el bloqueo de 30 días sigue vigente | Rechazo por RN-RES-08 | [resultado real] | [Conforme / investigar] |
| 19:02 | Diseño/Ejecución | Intentar una reserva WEB después de transcurridos más de 30 días desde la tercera inasistencia | El bloqueo debe haber expirado y la reserva WEB debe permitirse | HTTP 201, estado BORRADOR | Comportamiento conforme; el bloqueo expira después de 30 días |
| 19:04 | Diseño/Ejecución | Verificar que el bloqueo siga vigente antes de completar los 30 días | La reserva WEB debe rechazarse por RN-RES-08 | HTTP 409, código RES-08-BLOQUEO | Comportamiento conforme; el bloqueo permanece activo durante el período esperado |
| 19:14 | Diseño/Ejecución | Intentar crear una reserva un domingo | RN-RES-01 solo permite lunes a sábado, por lo que la reserva debe rechazarse | [resultado real] | [Conforme / posible defecto RN-RES-01] |

## Resumen de la sesión

La segunda sesión SBTM se concentró principalmente en explorar las reglas
temporales de anulación e inasistencia, especialmente RN-RES-05 a
RN-RES-08.

Durante la exploración se reprodujo inicialmente el defecto conocido de
cálculo de multas cubierto previamente por CP-04 / DEF-06. Al determinar
que continuar con combinaciones equivalentes no aportaría nueva
información, la sesión se redirigió hacia aspectos de RN-RES-08 no
cubiertos explícitamente por la suite diseñada.

La principal observación nueva se produjo al explorar la ventana móvil de
seis meses. Se construyó un historial con inasistencias el 02/03/2026,
01/09/2026 y 01/10/2026, evaluando el comportamiento el 19/10/2026.
La primera inasistencia ya se encontraba fuera de los últimos seis meses,
por lo que solo debían contabilizarse dos. Sin embargo, una nueva reserva
WEB fue rechazada con HTTP 409 y código RES-08-BLOQUEO.

Como investigación complementaria se comprobó que el bloqueo sí está
limitado al canal en línea: para el mismo paciente una reserva PRESENCIAL
fue aceptada con HTTP 201. También se verificó que el bloqueo permanece
vigente antes de completar los 30 días y que posteriormente permite
nuevamente realizar reservas WEB.

Se realizó además una comprobación de RN-RES-02 con una reserva superior
a 60 días de anticipación, la cual fue rechazada correctamente con
RES-02-ANTICIPACION.

Durante la sesión se hicieron adicionalmente algunas comprobaciones de
validación del registro de pacientes. Estas se conservan como evidencia
exploratoria secundaria, pero no forman parte de los hallazgos principales
del charter por no corresponder directamente a las RN-RES exploradas.

## Hallazgos

### S2-H01 — Cálculo incorrecto de la base de la multa

- Regla: RN-RES-06.
- Paciente: FONASA B.
- Arancel bruto: $25.000.
- Copago: $2.500.
- Esperado: multa del 30 % del copago = $750.
- Observado: multa de $7.500.
- Resultado: Falla.
- Cobertura previa: Sí, CP-04 / DEF-06.
- Clasificación: defecto conocido reproducido durante la sesión.

### S2-H02 — Inasistencia fuera de la ventana móvil es considerada para el bloqueo

- Regla: RN-RES-08.
- Historial:
  - 02/03/2026 → NO_ASISTIDA.
  - 01/09/2026 → NO_ASISTIDA.
  - 01/10/2026 → NO_ASISTIDA.
- Fecha de evaluación: 19/10/2026.
- Esperado: solo dos inasistencias pertenecen a los últimos seis meses,
  por lo que una reserva WEB debe permitirse.
- Observado: HTTP 409, código RES-08-BLOQUEO.
- Resultado: Falla.
- Cobertura previa: no cubierta explícitamente por la suite diseñada.
- Severidad propuesta: Alta.

## Controles conformes

- Anulación con más de cuatro horas de anticipación: multa $0.
- Bloqueo por inasistencias: WEB rechazado y PRESENCIAL permitido.
- Bloqueo aún vigente antes de completar los 30 días: HTTP 409.
- Reserva WEB después de superar los 30 días de bloqueo: HTTP 201.
- Reserva con más de 60 días de anticipación: rechazada por RN-RES-02.

## Observaciones

Se exploró también una diferencia de segundos alrededor del límite de
cuatro horas. Para el alcance adoptado se consideró consistente con la
granularidad en minutos utilizada por el diseño formal, por lo que no se
clasificó como defecto.

## Conclusión

La sesión logró complementar la suite diseñada encontrando un problema
que no surge al comprobar únicamente el caso nominal de tres
inasistencias. El defecto aparece al analizar qué eventos pertenecen
realmente a la ventana móvil de seis meses, mostrando el valor del
testing exploratorio para investigar interacciones temporales que no
habían sido representadas explícitamente en los casos formales.