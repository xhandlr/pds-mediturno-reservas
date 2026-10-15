# Charter SBTM — Sesión 2

## Identificación

- Sesión: SBTM-2
- Tester: Camilo Ñanco
- Duración planificada: 60 minutos
- Estado: Preparado antes de la ejecución
- Técnica: Session-Based Test Management (SBTM)

## Charter

Explorar el comportamiento de la anulación de reservas y de las
inasistencias alrededor de sus límites temporales, buscando
inconsistencias en el cálculo de multas, el bloqueo de reservas en
línea y la aplicación de las reglas RN-RES-05 a RN-RES-08.

## Objetivo

Evaluar de manera exploratoria comportamientos relacionados con:

- anulación con más de 4 horas de anticipación;
- anulación entre 4 horas y 1 hora;
- anulación con menos de 1 hora;
- cálculo de multas respecto del copago;
- registro de inasistencias;
- acumulación de inasistencias;
- ventana móvil de seis meses;
- bloqueo de reservas en línea durante 30 días.

El objetivo no es repetir sistemáticamente los casos de prueba
automatizados, sino explorar valores cercanos a los límites,
combinaciones y secuencias que puedan revelar comportamientos
no previstos por la suite diseñada.

## Reglas de negocio relacionadas

### RN-RES-05
Anulación con más de 4 horas de anticipación: sin costo.

### RN-RES-06
Anulación entre 4 horas y 1 hora: multa del 30 % del copago.

### RN-RES-07
Anulación con menos de 1 hora, o inasistencia: multa del 50 % del copago.

### RN-RES-08
3 inasistencias en 6 meses móviles: bloqueo de reserva en línea
por 30 días.

## Áreas de exploración previstas

### 1. Frontera de 4 horas

Explorar valores alrededor de la frontera entre RN-RES-05 y
RN-RES-06.

Ejemplos de valores de interés:

- superior a 4 horas;
- exactamente 4 horas;
- inmediatamente superior al límite;
- inmediatamente inferior al límite.

### 2. Frontera de 1 hora

Explorar valores alrededor de la frontera entre RN-RES-06 y
RN-RES-07.

Ejemplos:

- superior a 1 hora;
- exactamente 1 hora;
- inmediatamente inferior a 1 hora.

### 3. Base de cálculo de la multa

Comparar reservas de pacientes con distintas previsiones para
comprobar que los porcentajes establecidos por RN-RES-06 y
RN-RES-07 se apliquen sobre el copago correspondiente.

### 4. Inasistencias y bloqueo

Explorar el comportamiento después de registrar varias
inasistencias, considerando:

- cantidad de inasistencias;
- ventana móvil de seis meses;
- duración del bloqueo;
- canal de creación de una nueva reserva.

## Estrategia de ejecución

La sesión se realizará principalmente mediante la API pública de
ms-reservas.

Se utilizará `X-Reloj-Simulado` para controlar de forma determinista
los valores temporales explorados.

Las acciones se realizarán mediante endpoints públicos de creación,
confirmación, anulación e inasistencia.

Durante la sesión podrán surgir nuevas ideas de prueba derivadas de
los resultados obtenidos. Estas se registrarán en la bitácora.

## Oráculos

El comportamiento esperado se determinará utilizando como fuente
principal las reglas RN-RES-05 a RN-RES-08.

Una diferencia respecto de una regla explícita se registrará como
posible defecto.

Un comportamiento no definido claramente por las reglas se
registrará como observación o ambigüedad, no automáticamente como
defecto.

## Evidencia prevista

Durante la sesión se conservarán:

- hora de inicio y término;
- comandos ejecutados;
- datos relevantes utilizados;
- respuestas HTTP;
- actividades realizadas;
- observaciones;
- hallazgos;
- tiempo destinado a setup;
- tiempo de diseño y ejecución;
- tiempo de investigación de bugs.

## Criterio de finalización

La sesión finalizará después de al menos 60 minutos reales de
exploración y de realizar un debrief de los principales resultados.

Los hallazgos se trasladarán posteriormente a:

- matriz `Sesiones_SBTM`;
- bitácora de defectos, cuando corresponda;
- informe de Entrega 2;
- comparación SBTM vs. suite diseñada.