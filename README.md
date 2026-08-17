# MediTurno 2.0 — `ms-reservas`

Microservicio que implementa el **ciclo de vida completo de una hora médica**: creación,
confirmación, modificación, anulación, atención e inasistencia, según las reglas
**RN-RES-01 a RN-RES-10** del *Documento 1 — Enunciados de las Entregas*.

Este es el repositorio base de la **Entrega 2 (Pruebas Funcionales)**. El código ya existe:
tu equipo **no desarrolla funcionalidad de negocio**, diseña y automatiza la suite funcional
de caja negra que demuestra si ese código hace lo que la especificación dice.

> **Advertencia deliberada.** Este código está en producción en el escenario del curso y
> **contiene defectos reales**. No asumas que el comportamiento observado es el correcto:
> la única fuente de verdad son las reglas de negocio del Documento 1. Este README describe
> **cómo se comporta hoy el servicio**, no cómo debería comportarse. Donde el README y el
> Documento 1 difieran, la diferencia es un hallazgo que corresponde reportar.

---

## 1. Requisitos

| Herramienta | Versión | Verificar con |
|---|---|---|
| JDK | 17 o superior | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Docker | 24+ *(opcional)* | `docker --version` |
| Navegadores de Playwright | — | `mvn -B exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"` |

La base de datos es **H2 embebida**: no hay que instalar ni levantar nada.

## 2. Puesta en marcha

```bash
git clone <url-del-repositorio>
cd mediturno-ms-reservas

mvn -B test            # compila y ejecuta la suite (hoy vacía)
mvn spring-boot:run    # levanta el servicio
```

| Recurso | URL |
|---|---|
| Interfaz web | http://localhost:8082/ |
| API REST | http://localhost:8082/api/reservas |
| Documentación interactiva | http://localhost:8082/swagger |
| Consola de la base de datos | http://localhost:8082/h2 |

Con Docker:

```bash
docker compose up --build
```

### 2.1 Dos modos de base de datos

| Modo | Cómo se levanta | Comportamiento |
|---|---|---|
| **Persistente** *(por defecto)* | `mvn spring-boot:run` | Los datos sobreviven al reinicio (`datos/mediturno-reservas.mv.db`) |
| **Efímero** | `mvn spring-boot:run -Dspring-boot.run.profiles=efimero` | Base en memoria, se pierde al apagar |

El ambiente de referencia para la corrección es el **persistente**, porque es el que se parece
a producción. Una suite que solo pasa en el modo efímero no cumple el requisito de
idempotencia: significa que depende de partir de una base vacía.

## 3. El padrón de pacientes

Al primer arranque se cargan doce pacientes desde `src/main/resources/pacientes.csv`, elegidos
para cubrir las particiones de previsión, tramo, edad y convenio. Están ahí para exploración
rápida.

> Para que tu suite sea idempotente y pueda correr en paralelo, **cada escenario debe crear su
> propio paciente** con `POST /api/pacientes`. Compartir los pacientes del padrón entre
> escenarios genera dependencias de orden, que la rúbrica penaliza.

## 4. La hora del servicio

Casi todas las reglas interesantes dependen del tiempo: la ventana de anticipación
(RN-RES-02), el tramo de multa (RN-RES-05 a RN-RES-07) y la ventana de inasistencias
(RN-RES-08). Esperar cuatro horas para probar una multa no es una opción.

El servicio acepta la cabecera **`X-Reloj-Simulado`** con un instante ISO-8601. La hora
declarada rige **solo para esa petición**, de modo que la suite puede ejecutarse en paralelo
sin que un escenario le mueva el reloj a otro.

```bash
curl -s -X POST http://localhost:8082/api/reservas \
  -H 'Content-Type: application/json' \
  -H 'X-Reloj-Simulado: 2026-10-05T09:00:00' \
  -d '{
        "rutPaciente": "5.555.555-5",
        "especialidad": "MEDICINA_GENERAL",
        "fechaHoraAtencion": "2026-10-06T10:00:00",
        "canal": "WEB",
        "aceptaListaEspera": false
      }'
```

Respuesta `201 Created`:

```json
{
  "id": 1,
  "rutPaciente": "5.555.555-5",
  "especialidad": "MEDICINA_GENERAL",
  "fechaHoraAtencion": "2026-10-06T10:00:00",
  "canal": "WEB",
  "estado": "BORRADOR",
  "arancelBruto": 25000,
  "copago": 2500,
  "multa": 0,
  "modificaciones": 0
}
```

Sin la cabecera, el servicio usa la hora real de la máquina. Una suite que dependa de la hora
real es, por construcción, inestable: es el primer problema que hay que resolver.

## 5. La API

El contrato completo está en `docs/openapi.yaml` y publicado en `/swagger`.

| Verbo | Ruta | Qué hace |
|---|---|---|
| `POST` | `/api/reservas` | Crea la reserva. Queda en `BORRADOR`, o en `EN_ESPERA` si el bloque está ocupado y se aceptó lista de espera |
| `GET` | `/api/reservas` | Lista todas; con `?rut=` filtra por paciente |
| `GET` | `/api/reservas/{id}` | Detalle |
| `PATCH` | `/api/reservas/{id}` | Cambia la fecha y hora (RN-RES-09) |
| `POST` | `/api/reservas/{id}/confirmar` | Confirma |
| `POST` | `/api/reservas/{id}/iniciar` | Inicia la atención |
| `POST` | `/api/reservas/{id}/finalizar` | Marca la atención terminada |
| `POST` | `/api/reservas/{id}/anular` | Anula y calcula la multa |
| `POST` | `/api/reservas/{id}/inasistencia` | Registra que el paciente no llegó |
| `GET` `POST` | `/api/pacientes` | Padrón y alta de pacientes |

### 5.1 Errores

Todos los errores comparten el mismo cuerpo, y el **código es parte del contrato**: es estable
entre versiones, el mensaje no. Escribe las aserciones contra el código, no contra el texto.

```json
{
  "codigo": "RES-03-TOPE",
  "mensaje": "El paciente ya tiene el máximo de 3 reservas activas.",
  "estado": 409,
  "ruta": "/api/reservas",
  "instante": "2026-10-05T09:00:04.117"
}
```

| Código | Situación |
|---|---|
| `RES-01-DIA` `RES-01-BLOQUE` `RES-01-HORARIO` | El bloque pedido no existe en el calendario |
| `RES-02-PASADO` `RES-02-ANTICIPACION` | Fuera de la ventana de reserva |
| `RES-03-TOPE` | Tope de reservas activas del paciente |
| `RES-04-DUPLICADA` | Ya hay una reserva de esa especialidad para ese día |
| `RES-08-BLOQUEO` | Paciente bloqueado para reservar en línea |
| `RES-09-TOPE` `RES-09-ESTADO` | Modificación no admitida |
| `RES-10-TRANSICION` | Cambio de estado no permitido |
| `RES-CUPO` | El bloque quedó sin cupo |
| `RES-404` `PAC-404` | El recurso no existe |
| `PAC-DUPLICADO` | El RUT ya está registrado |
| `VAL-CAMPOS` `VAL-JSON` `VAL-ENUM` `VAL-TRAMO` | Entrada mal formada |

### 5.2 Esquemas para validación de contrato

En `src/test/resources/contratos/` están los JSON Schema del contrato, listos para RestAssured:

```java
given().header("X-Reloj-Simulado", "2026-10-05T09:00:00")
    .contentType(JSON).body(peticion)
.when().post("/api/reservas")
.then().statusCode(201)
    .body(matchesJsonSchemaInClasspath("contratos/reserva.schema.json"));
```

## 6. La interfaz web

| Página | Ruta |
|---|---|
| Listado de reservas | `/` |
| Nueva reserva | `/reservas/nueva` |
| Detalle y acciones | `/reservas/{id}` |
| Padrón de pacientes | `/pacientes` |

Los elementos relevantes llevan el atributo **`data-testid`**. Úsalos como localizador: son
estables entre versiones, a diferencia de las clases CSS y de la posición en el DOM.

```java
page.locator("[data-testid=input-rut]").fill("5.555.555-5");
page.locator("[data-testid=btn-crear]").click();
assertThat(page.locator("[data-testid=detalle-estado]")).hasText("BORRADOR");
```

Los XPath absolutos y las esperas fijas están **prohibidos** por el enunciado. Playwright ya
espera de forma automática; si necesitas una espera explícita, exprésala como condición sobre
un elemento, nunca como `Thread.sleep`.

## 7. Estructura

```
src/main/java/cl/losandes/mediturno/reservas/
├── dominio/          reglas de negocio en Java puro, sin framework
│   ├── EstadoReserva, MaquinaEstados       RN-RES-10
│   ├── CalendarioAtencion                  RN-RES-01
│   ├── PoliticaAnulacion                   RN-RES-05 a RN-RES-07
│   └── Reserva, Paciente, Tarifa, Especialidad, TramoFonasa, Canal
├── aplicacion/       orquestación: ReservaService (RN-RES-02, 03, 04, 08, 09)
├── infraestructura/  persistencia JPA, reloj y carga inicial
├── api/              controladores REST, DTO y manejo de errores
└── web/              controlador de la interfaz Thymeleaf

src/main/resources/
├── templates/        listado.html · nueva.html · detalle.html · pacientes.html
├── static/           estilos.css · logo.svg
└── pacientes.csv     padrón inicial

src/test/
├── java/             ← aquí escribes tu suite (hoy vacío, a propósito)
└── resources/
    ├── contratos/    JSON Schema del contrato de la API
    └── junit-platform.properties   ejecución paralela ya configurada

docs/openapi.yaml     contrato HTTP completo
```

## 8. Comandos de la Entrega 2

| Objetivo | Comando |
|---|---|
| Suite rápida (`*Test`) | `mvn -B test` |
| Suite de sistema, UI y API (`*IT`, runners de Cucumber) | `mvn -B verify` |
| Instalar navegadores de Playwright | `mvn -B exec:java -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install"` |
| Reporte de evidencia | `allure serve target/allure-results` |

Las dependencias de la entrega ya están declaradas en el `pom.xml` y no hay que agregarlas:
Cucumber 7, RestAssured 5 con `json-schema-validator`, Playwright, `axe-core` para Playwright
y Allure. El `junit-platform.properties` ya trae la ejecución paralela configurada.

## 9. Lo que este código NO hace

- **No hay pruebas.** `src/test/java` está vacío a propósito.
- **No hay autenticación.** Cualquiera puede operar cualquier reserva. Está fuera del alcance
  de esta entrega.
- **El sobrecupo del Release 2.1 no existe todavía.** Hoy cada bloque admite **una** reserva
  activa por especialidad; la segunda queda en lista de espera o se rechaza. El sobrecupo de
  hasta 2 pacientes por bloque llega en la Entrega 3.
- **No se llama a `ms-tarificacion`.** El copago se calcula localmente con las reglas RN-TAR
  del Documento 1. La integración entre ambos servicios es materia de la Entrega 3.

## 10. Antes de entregar

Revisa el checklist del Anexo C de la *Guía del Estudiante* y el de la plantilla de informe de
la Entrega 2. Los umbrales binarios de esta entrega son:

- cobertura **0-switch = 100 %** sobre la máquina de estados,
- mínimo **20 escenarios Gherkin** con `Scenario Outline` y `Examples`,
- **tres ejecuciones consecutivas con resultado idéntico**, sin dependencias de orden,
- conjunto **pairwise** generado con herramienta y justificado, no escrito a mano,
- auditoría de accesibilidad **WCAG 2.1 nivel AA** sobre los formularios críticos.
