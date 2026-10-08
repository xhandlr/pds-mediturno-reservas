# Selección de variables de alto riesgo para cobertura 3-wise


## Variables seleccionadas

### Anticipación

RN-RES relacionadas:

- RN-RES-02
- RN-RES-05
- RN-RES-06
- RN-RES-07

La anticipación presenta múltiples fronteras funcionales relevantes.
RN-RES-02 restringe las reservas en el pasado y a más de 60 días,
mientras que RN-RES-05, RN-RES-06 y RN-RES-07 modifican el
comportamiento de las multas en función del tiempo restante para la
atención.

Por esta razón se considera una variable de alto riesgo.

### Historial de inasistencias

RN-RES relacionada:

- RN-RES-08

RN-RES-08 establece un cambio de comportamiento cuando un paciente
alcanza tres inasistencias dentro de una ventana móvil de seis meses,
generando un bloqueo de reserva en línea durante 30 días.

Se considera de alto riesgo debido a que combina cantidad de eventos
previos y estado temporal del bloqueo.

### Canal

RN-RES relacionada:

- RN-RES-08

El canal es relevante porque RN-RES-08 establece específicamente un
bloqueo para la reserva en línea.

Por tanto, el comportamiento esperado depende de la interacción entre
el historial de inasistencias y el canal utilizado.

## Interacción 3-wise seleccionada

Se selecciona la siguiente interacción:

Anticipación × Historial de inasistencias × Canal

La cobertura pairwise garantiza que cada par de valores aparezca al menos
una vez, pero no garantiza que todas las combinaciones posibles de estas
tres variables aparezcan simultáneamente.

La cobertura 3-wise permitirá analizar interacciones que involucren al
mismo tiempo:

1. la condición temporal;
2. el historial de inasistencias del paciente;
3. el canal utilizado.


