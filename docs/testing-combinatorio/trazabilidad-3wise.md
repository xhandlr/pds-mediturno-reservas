# Cobertura 3-wise de variables de alto riesgo

## Herramienta

NIST ACTS — Automated Combinatorial Testing for Software.

## Fuerza de interacción

3-wise.

## Variables seleccionadas

| Variable | Valores | RN-RES |
|---|---:|---|
| Anticipación | 8 | RN-RES-02, RN-RES-05, RN-RES-06, RN-RES-07 |
| Historial de inasistencias | 4 | RN-RES-08 |
| Canal | 4 | RN-RES-08 |

## Justificación

Estas variables fueron seleccionadas debido a que concentran
comportamientos temporales, multas y restricciones de reserva en línea.

La cobertura 2-wise no garantiza que tres condiciones críticas aparezcan
simultáneamente en un mismo caso. Por ello se eleva esta interacción
específica a cobertura 3-wise.

## Modelo

`docs/testing-combinatorio/modelo-3wise-acts.txt`

## Resultado

`docs/testing-combinatorio/casos-3wise.csv`

## Configuración utilizada

Strength: 3

Comando:

java -Ddoi=3 -Doutput=csv -jar tools/acts/acts.jar \
docs/testing-combinatorio/modelo-3wise-acts.txt \
docs/testing-combinatorio/casos-3wise.csv
