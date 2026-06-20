# Linktic Full Stack — Microservicios Java + Vue.js

Solución Full Stack compuesta por dos microservicios independientes en Java Spring Boot y una aplicación Frontend en Vue.js 3, desarrollada como prueba técnica para Linktic.

---

## Tabla de Contenidos

1. [Arquitectura](#arquitectura)
2. [Tecnologías](#tecnologías)
3. [Decisiones Técnicas](#decisiones-técnicas)
4. [Estructura del Proyecto](#estructura-del-proyecto)
5. [Requisitos](#requisitos)
6. [Ejecución con Docker](#ejecución-con-docker)
7. [Ejecución en Desarrollo](#ejecución-en-desarrollo)
8. [APIs y Documentación](#apis-y-documentación)
9. [Testing](#testing)
10. [Git Flow](#git-flow)
11. [Uso de IA](#uso-de-ia)

---

## Arquitectura

```mermaid
flowchart TD
    FE["🖥️ Frontend Vue.js 3\n:3000 / :5173 dev"]
    PS["📦 products-service\n:8081 · Spring Boot 3.2"]
    IS["🏪 inventory-service\n:8082 · Spring Boot 3.2"]
    DB_P[("🐘 PostgreSQL\nproducts_db")]
    DB_I[("🐘 PostgreSQL\ninventory_db")]

    FE -- "X-API-Key\nGET /api/v1/products\nPOST /api/v1/products" --> PS
    FE -- "X-API-Key\nGET/PUT /api/v1/inventory\nPOST /api/v1/purchases" --> IS
    IS -- "X-API-Key + Spring Retry\nGET /api/v1/products/{id}" --> PS
    PS --- DB_P
    IS --- DB_I
```

---

## Flujo de Compra

```mermaid
sequenceDiagram
    actor U as Usuario
    participant FE as Frontend Vue.js
    participant IS as inventory-service :8082
    participant PS as products-service :8081
    participant DBI as PostgreSQL Inventario
    participant DBP as PostgreSQL Productos

    U->>FE: Selecciona producto + cantidad
    FE->>IS: POST /api/v1/purchases {productoId, cantidad}
    Note over IS: Verifica API Key

    IS->>PS: GET /api/v1/products/{id} + X-API-Key
    Note over IS,PS: Spring Retry — hasta 3 intentos (backoff 1s→2s→4s)
    PS->>DBP: SELECT * FROM products WHERE id = ?
    DBP-->>PS: Product row
    PS-->>IS: {id, nombre, precio, descripcion}

    IS->>DBI: SELECT * FROM inventory WHERE producto_id = ?
    DBI-->>IS: {cantidad: 50}

    alt Stock suficiente
        IS->>IS: Validar 50 >= 3 ✓
        IS->>DBI: UPDATE inventory SET cantidad = 47
        IS->>DBI: INSERT INTO purchases (...)
        DBI-->>IS: Purchase saved
        IS-->>FE: 200 {status: COMPLETED, total: 1049.97, ...}
        FE-->>U: ✅ Compra exitosa
    else Stock insuficiente
        IS-->>FE: 409 Conflict {message: "Stock insuficiente"}
        FE-->>U: ⚠️ Error de stock
    else Products-service no disponible
        IS-->>FE: 503 Service Unavailable
        FE-->>U: ⚠️ Servicio no disponible
    end
```

---

## Diagrama de Componentes Frontend

```mermaid
flowchart TD
    APP["App.vue"]
    NB["Navbar.vue"]
    ROUTER["Vue Router"]
    PV["ProductsView"]
    IV["InventoryView"]
    CV["PurchaseView"]

    PF["ProductForm.vue"]
    PC["ProductCard.vue"]
    IM["UpdateInventoryModal.vue"]
    CF["PurchaseForm.vue"]
    CR["PurchaseResult.vue"]
    LS["LoadingSpinner.vue"]
    EM["ErrorMessage.vue"]

    PSTORE["productStore (Pinia)"]
    ISTORE["inventoryStore (Pinia)"]
    PSVC["productService (Axios)"]
    ISVC["inventoryService (Axios)"]

    APP --> NB
    APP --> ROUTER
    ROUTER --> PV & IV & CV
    PV --> PF & PC & LS & EM
    IV --> IM & LS & EM
    CV --> CF & CR & EM

    PV & CV --> PSTORE
    IV & CV --> ISTORE
    PSTORE --> PSVC
    ISTORE --> ISVC
```

---

## Tecnologías

| Capa | Tecnología | Versión |
|------|-----------|---------|
| Backend | Java | 17 |
| Backend | Spring Boot | 3.2.5 |
| Backend | Spring Data JPA | 3.2.5 |
| Backend | Spring Security | 3.2.5 |
| Backend | Spring Retry | 2.x |
| Backend | PostgreSQL | 15 |
| Backend | Springdoc OpenAPI | 2.5.0 |
| Backend | Lombok | 1.18.x |
| Frontend | Vue.js | 3.4 |
| Frontend | Pinia | 2.1 |
| Frontend | Vue Router | 4.3 |
| Frontend | Axios | 1.7 |
| Frontend | Vite | 5.x |
| Testing Backend | JUnit 5 + Mockito | — |
| Testing Frontend | Vitest + Vue Test Utils | 1.6 |
| Infra | Docker + Docker Compose | — |

---

## Decisiones Técnicas

### Base de Datos: PostgreSQL (Relacional)

**Justificación:**
- El modelo de datos es claramente relacional: `producto_id` es referencia cruzada entre servicios.
- El flujo de compra requiere **transacciones ACID**: decrementar inventario y registrar Purchase deben ser atómicos.
- El esquema es estable y bien definido; NoSQL añadiría complejidad sin beneficio real.
- Patrón **Database per Service**: cada microservicio tiene su propia instancia PostgreSQL, garantizando independencia total.

### Flujo de Compra en `inventory-service`

El endpoint `POST /api/v1/purchases` reside en **inventory-service** porque:

1. **Atomicidad**: validar stock y decrementarlo ocurre en la misma transacción JPA — sin condiciones de carrera.
2. **Ownership de datos**: inventory-service es el _source of truth_ del stock.
3. **Baja latencia**: la validación accede directamente a su DB local.
4. El products-service solo se consulta para validar existencia y obtener precio (dato de solo lectura).

### Comunicación entre Servicios

- **Protocolo**: HTTP REST con `RestTemplate` (síncrono, adecuado para carga transaccional).
- **Autenticación**: Header `X-API-Key` en todas las llamadas inter-servicio.
- **Resiliencia**: Spring Retry — 3 intentos, backoff exponencial 1s → 2s → 4s.
- **Timeouts**: configurable via `PRODUCTS_SERVICE_TIMEOUT` (default 5s), implementado en `SimpleClientHttpRequestFactory`.
- **Error handling**: si products-service no responde tras 3 reintentos → 503 con mensaje descriptivo.

### Seguridad: API Key Filter

- `ApiKeyFilter` (extiende `OncePerRequestFilter`) aplicado antes de Spring Security.
- Rutas públicas: `/swagger-ui/**`, `/api-docs/**`, `/actuator/**`.
- Todas las rutas `/api/**` requieren `X-API-Key` válida.
- Las claves se configuran exclusivamente via variables de entorno.

---

## Estructura del Proyecto

```
Prueba Tecnica Linktic/
├── products-service/
│   ├── src/main/java/com/linktic/products/
│   │   ├── config/          # SecurityConfig, OpenApiConfig
│   │   ├── controller/      # ProductController
│   │   ├── dto/             # ProductDTO, CreateProductRequest, ApiResponse
│   │   ├── exception/       # ProductNotFoundException, GlobalExceptionHandler
│   │   ├── model/           # Product (entity)
│   │   ├── repository/      # ProductRepository
│   │   ├── security/        # ApiKeyFilter
│   │   └── service/impl/    # ProductServiceImpl
│   ├── src/test/            # Unit + Integration tests
│   ├── Dockerfile
│   └── pom.xml
│
├── inventory-service/
│   ├── src/main/java/com/linktic/inventory/
│   │   ├── client/          # ProductsClient + ProductsClientImpl
│   │   ├── config/          # SecurityConfig, OpenApiConfig, RestTemplateConfig
│   │   ├── controller/      # InventoryController
│   │   ├── dto/             # InventoryDTO, PurchaseRequest, PurchaseResultDTO…
│   │   ├── exception/       # InventoryNotFoundException, InsufficientStockException…
│   │   ├── model/           # Inventory, Purchase (entities)
│   │   ├── repository/      # InventoryRepository, PurchaseRepository
│   │   ├── security/        # ApiKeyFilter
│   │   └── service/impl/    # InventoryServiceImpl
│   ├── src/test/            # Unit + Integration tests
│   ├── Dockerfile
│   └── pom.xml
│
├── frontend/
│   ├── src/
│   │   ├── assets/          # main.css (design tokens + estilos globales)
│   │   ├── components/
│   │   │   ├── layout/      # Navbar
│   │   │   ├── products/    # ProductCard, ProductForm
│   │   │   ├── inventory/   # UpdateInventoryModal
│   │   │   ├── purchase/    # PurchaseForm, PurchaseResult
│   │   │   └── shared/      # LoadingSpinner, ErrorMessage
│   │   ├── router/          # Vue Router (lazy loading)
│   │   ├── services/        # api.js (Axios + interceptors), productService, inventoryService
│   │   ├── stores/          # productStore, inventoryStore (Pinia)
│   │   └── views/           # ProductsView, InventoryView, PurchaseView
│   ├── tests/unit/          # Vitest + Vue Test Utils
│   ├── Dockerfile
│   └── nginx.conf
│
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## Requisitos

- Docker 24+ y Docker Compose v2
- *(Desarrollo local)* Java 17+, Maven 3.9+, Node.js 20+

---

## Ejecución con Docker

```bash
# 1. Clonar el repositorio
git clone git@github.com:IngenieroRicardoGordillo/prueban-tecnica-hawy-gordillo.git
cd prueban-tecnica-hawy-gordillo

# 2. Configurar variables de entorno
cp .env.example .env
# Opcional: editar .env con tus API keys personalizadas

# 3. Levantar todos los servicios
docker compose up --build

# Servicios disponibles:
# Frontend:           http://localhost:3000
# Products Service:   http://localhost:8081
# Inventory Service:  http://localhost:8082
# Swagger Products:   http://localhost:8081/swagger-ui.html
# Swagger Inventory:  http://localhost:8082/swagger-ui.html

# Detener
docker compose down

# Detener y eliminar volúmenes
docker compose down -v
```

---

## Ejecución en Desarrollo

### Products Service

```bash
cd products-service
export PRODUCTS_DB_URL=jdbc:postgresql://localhost:5432/products_db
export PRODUCTS_DB_USER=postgres
export PRODUCTS_DB_PASSWORD=postgres
export PRODUCTS_API_KEY=dev-products-key
mvn spring-boot:run
# → http://localhost:8081
```

### Inventory Service

```bash
cd inventory-service
export INVENTORY_DB_URL=jdbc:postgresql://localhost:5433/inventory_db
export INVENTORY_DB_USER=postgres
export INVENTORY_DB_PASSWORD=postgres
export INVENTORY_API_KEY=dev-inventory-key
export PRODUCTS_SERVICE_URL=http://localhost:8081
export PRODUCTS_API_KEY=dev-products-key
mvn spring-boot:run
# → http://localhost:8082
```

### Frontend

```bash
cd frontend
cp .env.example .env
npm install
npm run dev
# → http://localhost:5173
```

---

## APIs y Documentación

Swagger UI disponible en cada servicio (sin API Key requerida para acceder a la UI):

| Servicio | Swagger UI |
|----------|-----------|
| Products | http://localhost:8081/swagger-ui.html |
| Inventory | http://localhost:8082/swagger-ui.html |

### Products Service

**Header requerido:** `X-API-Key: <PRODUCTS_API_KEY>`

| Método | Ruta | Descripción | Códigos |
|--------|------|-------------|---------|
| `POST` | `/api/v1/products` | Crear producto | 201, 400, 401 |
| `GET` | `/api/v1/products` | Listar todos | 200, 401 |
| `GET` | `/api/v1/products/{id}` | Obtener por ID | 200, 401, 404 |

**Ejemplo — Crear producto:**
```json
POST /api/v1/products
Headers: X-API-Key: products-api-key-change-in-production

{
  "nombre": "Laptop Dell XPS 15",
  "precio": 2499.99,
  "descripcion": "Laptop de alto rendimiento con i9"
}
```

### Inventory Service

**Header requerido:** `X-API-Key: <INVENTORY_API_KEY>`

| Método | Ruta | Descripción | Códigos |
|--------|------|-------------|---------|
| `GET` | `/api/v1/inventory` | Listar todo el inventario | 200, 401 |
| `GET` | `/api/v1/inventory/{productoId}` | Consultar por producto | 200, 401, 404 |
| `PUT` | `/api/v1/inventory/{productoId}` | Crear/actualizar cantidad | 200, 400, 401 |
| `POST` | `/api/v1/purchases` | Realizar compra | 200, 400, 401, 404, 409, 503 |

**Ejemplo — Realizar compra:**
```json
POST /api/v1/purchases
Headers: X-API-Key: inventory-api-key-change-in-production

{
  "productoId": "550e8400-e29b-41d4-a716-446655440000",
  "cantidad": 2
}
```

### Formato de Respuesta (envelope)

```json
{
  "success": true,
  "data": { "..." },
  "message": "Operación exitosa",
  "timestamp": "2026-06-19T10:30:00"
}
```

Error:
```json
{
  "success": false,
  "message": "Stock insuficiente. Solicitado: 100, Disponible: 47",
  "timestamp": "2026-06-19T10:30:00"
}
```

---

## Testing

### Backend

```bash
cd products-service && mvn test
cd inventory-service && mvn test
```

Tests usan H2 in-memory — no requieren PostgreSQL local.

| Suite | Tipo | Tests |
|-------|------|-------|
| `ProductServiceTest` | Unitario (Mockito) | 5 |
| `ProductControllerIntegrationTest` | Integración (MockMvc + H2) | 5 |
| `InventoryServiceTest` | Unitario (Mockito) | 6 |
| `InventoryControllerIntegrationTest` | Integración (MockMvc + H2 + MockBean) | 5 |

### Frontend

```bash
cd frontend
npm install
npm test              # una pasada
npm run test:watch    # modo watch
npm run test:coverage # con cobertura
```

| Suite | Tests |
|-------|-------|
| `productService.test.js` | 5 |
| `inventoryService.test.js` | 5 |
| `ProductForm.test.js` | 6 |
| `PurchaseForm.test.js` | 7 |

---

## CI/CD

Los pipelines de integración y entrega continua están implementados en `.github/workflows/`:

| Archivo | Trigger | Qué hace |
|---------|---------|----------|
| `ci.yml` | PR o push a cualquier rama | Ejecuta los tests de `products-service`, `inventory-service` y `frontend` en paralelo |
| `cd.yml` | Merge a `main` | Construye las 3 imágenes Docker localmente en el runner (`push: false`) y publica un resumen en GitHub Actions |

> **Nota sobre estado de los pipelines en este repositorio:**
> Ambos workflows están correctamente configurados y son válidos, pero fallan en GitHub Actions debido a una **restricción de facturación a nivel de cuenta** que bloquea la ejecución de runners antes de que arranquen (los checks fallan en 1-3 segundos, no por errores de código ni tests).
>
> **Decisiones tomadas en consecuencia:**
> - **CI (`ci.yml`):** Se mantiene sin cambios. Los tests se pueden verificar ejecutando `mvn test` en cada servicio y `npm test` en el frontend — todos pasan localmente (ver sección [Testing](#testing)).
> - **CD (`cd.yml`):** Se modificó de `push: true` a `push: false`. El build de las imágenes Docker valida que los `Dockerfile` son correctos y que las dependencias resuelven; el paso de push a GHCR es solo transporte y no aporta valor de validación adicional. Para habilitarlo en producción basta con cambiar esa bandera y agregar los secrets del registry, sin tocar el código.
>
> Los archivos de workflow reflejan la intención y arquitectura CI/CD completa; la restricción es operacional, no técnica.

---

## Git Flow

Este proyecto sigue el modelo **Git Flow**:

```
main          ←──── release/1.0.0 ←──── develop
                                          ↑
                          ┌───────────────┼───────────────┐
                    feature/          feature/         feature/
                 products-service  inventory-service  frontend-vue  ...
```

| Rama | Propósito |
|------|-----------|
| `main` | Código en producción, siempre estable. Tagged con versión. |
| `develop` | Integración de features. Base para nuevas ramas. |
| `feature/products-service` | Microservicio de productos |
| `feature/inventory-service` | Microservicio de inventario |
| `feature/frontend-vue` | Aplicación Vue.js 3 |
| `feature/docker-devops` | Docker Compose e infraestructura |
| `feature/documentation` | README, diagramas, decisiones técnicas |
| `release/1.0.0` | Preparación de release, merge a `main` con tag `v1.0.0` |

---

## Uso de IA

Este proyecto fue desarrollado con asistencia de **Claude Sonnet 4.6** (Anthropic) a través de **Claude Code**, usado como acelerador de desarrollo — no como sustituto del criterio técnico.

### Cómo se usó

La IA se empleó para scaffolding inicial, generación de boilerplate repetitivo y como segundo par de ojos para detectar inconsistencias. En ningún caso se aceptó código sin revisarlo y comprenderlo.

### Decisiones que tomé yo (no la IA)

- **¿Dónde vive `POST /purchases`?** La IA propuso inicialmente ponerlo en products-service. Lo moví a inventory-service porque la atomicidad de validar stock + decrementar + registrar compra requiere una sola transacción JPA sobre la misma base de datos. Explicarlo fue criterio propio.
- **Spring Retry vs Resilience4j:** La IA sugirió Resilience4j. Elegí Spring Retry porque ya es parte del ecosistema Spring Boot, no agrega dependencias pesadas y cubre exactamente el caso de uso (reintentos con backoff). Para circuit breaker completo sí usaría Resilience4j.
- **`saveAndFlush()` en lugar de `save()`:** El código generado usaba `save()`. Al revisar los tests noté que `createdAt` y `updatedAt` llegaban `null` en la respuesta. Investigué: `@CreationTimestamp` lo popula Hibernate al hacer flush, no al devolver la entidad en memoria. Corregí a `saveAndFlush()` en todos los servicios.
- **Health checks con `wget` en lugar de `curl`:** Docker Compose generado usaba `curl`. Al construir la imagen noté que `eclipse-temurin:17-jre-alpine` no incluye curl (imagen mínima). Reemplacé por `wget -qO-` que sí trae BusyBox Alpine. Los contenedores pasaron de `unhealthy` a `healthy`.
- **Patrón Database per Service:** La IA no lo propuso explícitamente. Lo apliqué conscientemente para garantizar independencia total entre microservicios — cada uno tiene su propia instancia PostgreSQL.

### Qué corregí del output generado

| Problema detectado | Corrección aplicada |
|--------------------|---------------------|
| `save()` → timestamps `null` en respuesta | Cambié a `saveAndFlush()` en todos los repositorios |
| Health check con `curl` (no disponible en Alpine JRE) | Reemplazado por `wget -qO-` |
| JSON de error 401 construido como `String` manual en `ApiKeyFilter` | Refactorizado a `ObjectMapper.writeValue()` para consistencia con el resto de la API |
| `version: '3.8'` en docker-compose.yml (deprecado en Compose v2) | Eliminado el atributo `version` |
| `ddl-auto: update` sin contexto | Documentado con comentario: aceptable para prueba técnica, en producción requiere Flyway/Liquibase |

### Limitación de seguridad conocida

La comparación de API keys usa `String.equals()`:

```java
if (!apiKey.equals(providedKey)) { ... }
```

Esto es suficiente para el contexto de esta prueba técnica. En producción, para APIs expuestas a internet se usaría comparación en tiempo constante (`MessageDigest.isEqual()`) para evitar timing attacks. Se documenta como decisión consciente, no como omisión.

### Validación del código generado

- Todos los tests de integración se ejecutaron contra H2 real (no mocks de repositorio).
- Se verificó el flujo completo con `docker compose up --build` y pruebas manuales en Swagger UI.
- Se revisó que la seguridad (`ApiKeyFilter`) bloqueara correctamente rutas protegidas y permitiera Swagger/actuator sin clave.
- Los diagramas Mermaid se validaron visualmente en GitHub antes de dar por terminada la documentación.
