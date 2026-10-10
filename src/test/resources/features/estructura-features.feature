# Diseño de estructura de Features Gherkin

## Objetivo

Definir la organización de las especificaciones Gherkin de MediTurno
antes de implementar los step definitions, agrupando los escenarios
según comportamiento funcional y manteniendo trazabilidad con las
reglas RN-RES.

## Estructura

src/test/resources/features/
└── reservas/
    ├── reserva_hora.feature
    ├── anulacion_inasistencia.feature
    ├── modificacion_reserva.feature
    └── ciclo_vida_reserva.feature

## Distribución funcional

| Feature | RN-RES | Origen principal | Escenarios previstos |
|---|---|---|---:|
| reserva_hora.feature | RN-RES-01 a RN-RES-04 | Pairwise, particiones y valores límite | 6 |
| anulacion_inasistencia.feature | RN-RES-05 a RN-RES-08 | Tabla de decisión, límites, pairwise y 3-wise | 7 |
| modificacion_reserva.feature | RN-RES-09 | Particiones y valores límite | 3 |
| ciclo_vida_reserva.feature | RN-RES-10 | Máquina de estados, 0-switch y 1-switch | 5 |

Total previsto: 21 escenarios.

## Criterios de diseño

1. Los archivos se organizan por comportamiento funcional y no por
   pantalla, endpoint o tecnología de automatización.

2. Los escenarios utilizarán lenguaje de negocio y no describirán clics,
   selectores, requests HTTP ni detalles de implementación.

3. Los Scenario Outline utilizarán Examples derivados de los artefactos
   de diseño existentes, principalmente pairwise, valores límite y tabla
   de decisión.

4. Los mismos escenarios de negocio podrán ser reutilizados posteriormente
   por la automatización API y UI.

5. Cada escenario conservará trazabilidad con una o más reglas RN-RES.

6. Las transiciones no especificadas por RN-RES-10 serán registradas como
   hallazgos y no se considerarán automáticamente comportamiento esperado.

7. Los step definitions no forman parte de esta etapa. Se implementarán
   después de estabilizar las especificaciones Gherkin.

## Convención de tags

Se utilizarán tags funcionales y de trazabilidad:

- `@RN_RES_01` ... `@RN_RES_10`
- `@pairwise`
- `@limite`
- `@decision`
- `@estado`
- `@3wise`

Los tags describen el origen y la regla cubierta, no la interfaz desde
la que se ejecutará la prueba.