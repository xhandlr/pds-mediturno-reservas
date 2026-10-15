# Análisis de reducción y riesgo residual

## 1. Espacio exhaustivo

El modelo combinatorio considera ocho variables:

| Variable | Particiones |
|---|---:|
| Previsión | 4 |
| Especialidad | 5 |
| Canal | 4 |
| Anticipación | 8 |
| Historial de inasistencias | 4 |
| Edad | 3 |
| Convenio | 2 |
| Cupo del bloque | 3 |

El producto cartesiano teórico es:

4 × 5 × 4 × 8 × 4 × 3 × 2 × 3 = 46.080

Por tanto, una estrategia exhaustiva requeriría evaluar
46.080 combinaciones.

## 2. Reducción mediante pairwise

El conjunto 2-wise fue generado mediante allpairspy y contiene
46 casos.

La reducción respecto del espacio exhaustivo se calcula como:

Reducción = (1 - 46 / 46.080) × 100

Reducción = 99,9002 %

Por tanto:

- Espacio exhaustivo: 46.080 combinaciones
- Pairwise: 46 casos
- Combinaciones evitadas respecto del exhaustivo: 46.034
- Reducción obtenida: 99,90 %
- Porcentaje conservado: aproximadamente 0,10 %

El conjunto pairwise permite cubrir las interacciones entre pares
de valores sin ejecutar todo el producto cartesiano.

## 3. Refuerzo 3-wise

La cobertura 2-wise no garantiza que una combinación de tres
condiciones aparezca simultáneamente en un mismo caso.

Se identificaron como variables de mayor riesgo:

- Anticipación
- Historial de inasistencias
- Canal

Estas variables se encuentran asociadas principalmente con
RN-RES-02 y RN-RES-05 a RN-RES-08.

El subespacio de interacciones 3-wise seleccionado contiene:

8 × 4 × 4 = 128 combinaciones

Estas 128 combinaciones representan las interacciones triples que
se deben cubrir entre las variables de alto riesgo y no deben sumarse 
directamente los 46 casos pairwise y las 128 combinaciones 3-wise para 
afirmar que existen 174 casos finales,ya que las combinaciones 3-wise 
representan objetivos de cobertura sobre tres variables y posteriormente 
deben integrarse en casos completos del modelo.


## 4. Justificación de la selección 3-wise

La anticipación presenta múltiples fronteras de negocio:

- reservas en el pasado;
- máximo de 60 días;
- límite de 60 minutos;
- límite de 240 minutos.

El historial de inasistencias modifica el comportamiento al
alcanzar tres inasistencias dentro de seis meses y puede provocar
un bloqueo temporal.

El canal es relevante porque RN-RES-08 afecta específicamente la
posibilidad de reservar en línea.

Una falla podría depender, por ejemplo, de la interacción simultánea:

Canal WEB
+
3 inasistencias en seis meses
+
menos de 60 minutos de anticipación

Aunque cada par pueda estar cubierto individualmente por el
conjunto pairwise, esto no garantiza que las tres condiciones
aparezcan juntas.

Por este motivo se refuerza específicamente esta interacción
mediante cobertura 3-wise.

## 5. Riesgo residual

A pesar de la cobertura 2-wise y del refuerzo 3-wise seleccionado,
continúan existiendo clases de defectos que podrían quedar fuera
del conjunto combinatorio.

### 5.1 Interacciones de tres variables no seleccionadas

El refuerzo 3-wise se concentra en Anticipación,
HistorialInasistencias y Canal.

Por tanto, otras interacciones triples no están garantizadas, por
ejemplo:

Previsión × Edad × Convenio

o:

Especialidad × Anticipación × CupoBloque

El riesgo se acepta porque estas interacciones se consideran de
menor prioridad respecto de las variables seleccionadas.

### 5.2 Interacciones de cuatro o más variables

Ni pairwise ni el refuerzo 3-wise seleccionado garantizan la
cobertura completa de interacciones que requieran cuatro o más
condiciones simultáneas.

Cubrirlas exhaustivamente aumentaría considerablemente el número
de casos y reduciría el beneficio del testing combinatorio.

### 5.3 Comportamiento secuencial

El modelo combinatorio representa combinaciones de entradas, pero
no sustituye las pruebas de transición de estados.

Los defectos asociados a secuencias como:

BORRADOR -> CONFIRMADA -> EN_CURSO -> ATENDIDA

o sus transiciones alternativas deben cubrirse mediante el modelo
de estados correspondiente a RN-RES-10.

### 5.4 Reglas dependientes del historial

Reglas como RN-RES-03, RN-RES-08 y RN-RES-09 dependen del estado
acumulado o de acciones previas.

Una combinación estática de parámetros no representa por sí sola
toda la secuencia necesaria para verificar estas reglas.

### 5.5 Entradas inválidas o malformadas

El modelo combinatorio utiliza clases funcionales válidas.

Casos como:

- campos ausentes;
- tipos de datos incorrectos;
- valores no reconocidos;
- inyección;
- payload sobredimensionado;

se abordarán posteriormente mediante pruebas negativas de API.
