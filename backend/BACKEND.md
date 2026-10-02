# Backend — Spring Boot API

The Java service that owns the relational model of the flight network. Exposes a REST API for airports, airlines, routes, hubs, and statistics; imports the OpenFlights dataset at startup; and proxies graph-algorithm requests to the Python graph service.

Part of the [FlightNetworkExplorer](../README.md) project.

---

## Overview

- **Language / runtime:** Java 21
- **Framework:** Spring Boot 3.4.x
- **Persistence:** Spring Data JPA + Hibernate
- **Database:** H2 (file-based, development); PostgreSQL-ready
- **HTTP client:** Spring `RestClient` for calling the graph service
- **Build:** Maven wrapper (`./mvnw`)

## Requirements

- **JDK 21 or newer** — `java --version`
- **Lombok 1.18.40+** — set in `pom.xml`, required for JDK 25 compatibility
- The graph service is **not** required at startup, but `/api/graph/*`, `/api/routes/compare/*`, and `/api/hubs` (indirectly) need it at request time

## Running

```bash
# From the backend/ directory
./mvnw spring-boot:run
```

First start runs three importers and writes to `backend/data/flights.mv.db`:

```
Airport import completed: 7698 imported, 0 skipped
Airline import completed: 6162 imported, 0 skipped, ...
Route import completed: 67663 imported, 0 skipped, NNNNN with distance
Started BackendApplication in ~10 seconds
```

Subsequent starts skip the import (~2 seconds).

The H2 console is available at **http://localhost:8080/h2-console** (JDBC URL `jdbc:h2:file:./data/flights`, user `sa`, empty password).

## Configuration

`backend/src/main/resources/application.properties`:

| Property | Default | Purpose |
|---|---|---|
| `server.port` | `${PORT:8080}` | HTTP port |
| `spring.datasource.url` | `jdbc:h2:file:./data/flights` | H2 database location |
| `spring.jpa.hibernate.ddl-auto` | `update` | Schema management |
| `spring.h2.console.enabled` | `true` | H2 web console (dev only) |
| `airports.file` | `${AIRPORTS_FILE:../data/raw/airports.dat}` | Airport source CSV |
| `airlines.file` | `${AIRLINES_FILE:../data/raw/airlines.dat}` | Airline source CSV |
| `routes.file` | `${ROUTES_FILE:../data/raw/routes.dat}` | Route source CSV |
| `graph.service.url` | `${GRAPH_SERVICE_URL:http://localhost:8000}` | Graph service base URL |
| `graph.service.connect-timeout` | `2s` | Connect timeout for graph calls |
| `graph.service.read-timeout` | `5s` | Read timeout for graph calls |

All env vars can be overridden at runtime — useful for containerised deployment.

## Data model

Three JPA entities, all flat (no associations):

### `Airport`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | OpenFlights airport ID (externally assigned) |
| `name` | `String` | Required |
| `city`, `country` | `String` | Nullable |
| `iata` | `String` | 3 letters, unique, nullable (some airports lack IATA) |
| `icao` | `String` | 4 letters, unique, nullable |
| `latitude`, `longitude` | `double` | WGS84 coordinates |

### `Airline`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | OpenFlights airline ID |
| `name` | `String` | Required |
| `alias` | `String` | Nullable |
| `iata` | `String` | 2–3 alphanumeric, nullable |
| `icao` | `String` | 4 letters, nullable |
| `country` | `String` | Nullable |
| `active` | `String` | `"Y"` or `"N"` |

### `Route`

| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated |
| `airline` | `String` | Single IATA code (one row per airline-route pair) |
| `sourceIata`, `destinationIata` | `String` | 3 letters, required |
| `distanceKm` | `Double` | Computed at import via Haversine |

**No associations between entities** — this is deliberate. The routes table stores IATA codes as plain strings, not foreign keys. The graph is modelled separately in the Python service, and the relational side doesn't need to walk object graphs.

## Importers

Three `ApplicationRunner` beans load the dataset on startup. Each is idempotent (skips if the table is non-empty) and runs inside a transaction.

| Importer | Order | Loads |
|---|---|---|
| `AirportImportService` | 1 | 7,698 airports |
| `AirlineImportService` | 2 | 6,162 airlines |
| `RouteImportService` | 3 | 67,663 routes + computes distance |

**Ordering** (`@Order(1/2/3)`) ensures airports are loaded before routes, so `RouteImportService` can compute Haversine distances using airport coordinates.

**Null handling:** OpenFlights uses `\N` as its null marker, and some fields contain escaped-apostrophe artifacts (`\\'`). Both are normalised to `null` during import. IATA and ICAO codes are validated strictly — anything that doesn't match the expected format is dropped, not stored as garbage.

### Re-importing

Delete the H2 database and restart:

```bash
rm backend/data/flights.mv.db backend/data/flights.trace.db
./mvnw spring-boot:run
```

## Package structure

```
com.backend.backend/
├── BackendApplication.java          @SpringBootApplication, @EnableCaching
├── client/                          GraphServiceClient (RestClient wrapper)
├── config/                          CorsConfig, GraphServiceConfig, GraphServiceProperties
├── controller/                      REST endpoints + GlobalExceptionHandler
├── dto/                             Response records (no entities leak to the wire)
├── exception/                       Domain exceptions (404/400/503 mappings)
├── model/                           JPA entities
├── repository/                      Spring Data repositories
├── service/                         Business logic + importers
└── util/                            GeoUtils (Haversine)
```

## API reference

All endpoints return JSON. Errors return a consistent body:

```json
{
  "timestamp": "2026-10-02T06:45:13Z",
  "status": 404,
  "error": "Not Found",
  "message": "Airport not found: XXX",
  "path": "/api/airports/XXX"
}
```

### Airports

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/airports` | All airports (no pagination) |
| `GET` | `/api/airports/{iata}` | Single airport. 404 if missing. |

### Network

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/network/{iata}` | Direct outbound network — nodes + edges, deduplicated |

### Statistics

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/airport/{iata}/stats` | Connections, in/out route counts, top destinations, airlines |

Counts are **distinct** — a route flown by three airlines counts once.

### Hubs

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/hubs?limit=N` | Top N airports by combined departure + arrival connectivity. `limit` 1–500, default 150. |

Results are cached (`@Cacheable("hubs", key="#limit")`).

### Airlines

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/airlines?page=&size=` | Paginated airline list. `size` 1–200, default 50. |
| `GET` | `/api/airlines/{iata}` | Airline by IATA. 404 if missing. |
| `GET` | `/api/airlines/{iata}/routes?page=&size=` | Routes operated by this airline, paginated. |

### Routes

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/routes/{from}/{to}` | Direct route details — distance, time, airlines |
| `GET` | `/api/routes/compare/{from}/{to}` | Route comparison — up to 10 alternatives, ranked |

The comparison endpoint calls the graph service for path alternatives, then enriches each with per-leg distances and airlines from the database.

### Graph (pass-through)

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/graph/connections/{iata}` | Direct successors of a node |
| `GET` | `/api/graph/path/{from}/{to}` | Shortest path by distance |
| `GET` | `/api/graph/centrality?metric=&limit=` | Centrality ranking. `metric` is `degree`, `betweenness`, or `closeness`. |

These proxy the Python graph service. If the graph service is unreachable, they return **503** with a clear message.

## Error handling

A single `@RestControllerAdvice` (`GlobalExceptionHandler`) maps domain exceptions to HTTP responses:

| Exception | HTTP status |
|---|---|
| `AirportNotFoundException` | 404 |
| `AirlineNotFoundException` | 404 |
| `IllegalArgumentException` | 400 |
| `ConstraintViolationException` (bean validation) | 400 |
| `GraphServiceUnavailableException` | 503 |
| Any other `Exception` | 500 (with logging) |

Domain exceptions are thrown from services and controllers; the handler builds a consistent error body.

## Caching

Two methods are cached with Spring's in-memory `ConcurrentMapCacheManager`:

| Cache | Method | Key |
|---|---|---|
| `hubs` | `HubService.getMajorHubs(int)` | `#limit` |
| `network` | `NetworkService.getNetwork(String)` | `#iata.trim().toUpperCase()` |

Caching is enabled via `@EnableCaching` on `BackendApplication`. Data doesn't change during a session, so no eviction is configured.

Empirically, the second call to `/api/hubs?limit=10` is **~65× faster** than the first.

## Graph service integration

`GraphServiceClient` wraps a `RestClient` bean configured in `GraphServiceConfig`:

- **Timeouts:** 2s connect, 5s read
- **HTTP version:** HTTP/1.1 forced (JDK's HTTP/2 upgrade negotiation breaks with uvicorn)
- **404 handling:** empty result returned, not thrown — a missing path is a valid response
- **5xx handling:** rethrown as `GraphServiceUnavailableException` → 503

Response DTOs (`GraphConnectionsResponse`, `GraphPathResponse`, `GraphPathsResponse`, `GraphCentralityResponse`) are records in `dto/`.

## Testing

`spring-boot-starter-test` is on the classpath. `BackendApplicationTests` exists as a context-load smoke test.

Suggested priorities for adding tests:

1. **Importers** — they're the source of truth for all data, and we've hit two bugs there (`\N` handling, column indices)
2. **`NetworkService`** — the core expansion logic
3. **`HubService`** — ranking and caching
4. **`GraphServiceClient`** — mockable with `MockRestServiceServer`
5. **Controller slice tests** — `@WebMvcTest` per controller

Run: `./mvnw test`

## Troubleshooting

### `cannot find symbol: method getIata()`

Lombok isn't running. Check:

```bash
./mvnw dependency:tree | grep lombok
```

The `maven-compiler-plugin` in `pom.xml` must have an explicit `<version>` on the Lombok `<path>` — versionless paths don't inherit from dependency management.

### `java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN`

Lombok version doesn't support your JDK. Lombok **1.18.40+** supports JDK 25. Check with `java --version`.

### `Wrong user name or password` (H2)

The H2 file was created with different credentials. Either add `spring.datasource.username` and `spring.datasource.password` to `application.properties`, or delete `backend/data/flights.mv.db` and let it rebuild.

### `Database may be already in use`

Another backend process holds the lock. Find it with `lsof -i :8080` and kill it, or delete the `.mv.db` and restart.

### `503` on `/api/graph/*`

The Python graph service isn't running. Start it:

```bash
cd ../graph-service
uvicorn app.main:app --reload --port 8000
```

### Routes table has numeric codes instead of IATA

The import column indices are wrong. See `RouteImportService.java` — IATA is columns **2** and **4**, not 3 and 5. Delete the DB and re-import.

---

*Part of [FlightNetworkExplorer](../README.md). See also the [graph service](../graph-service/README.md) and [frontend](../frontend/README.md).*