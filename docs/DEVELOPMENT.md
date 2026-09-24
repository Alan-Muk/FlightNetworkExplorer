# Development Workflow

Reference for bringing up the full FlightNetworkExplorer stack and verifying
that it works.

## Services

| Service | Port | Language |
|---------|------|----------|
| Graph service | 8000 | Python (FastAPI + NetworkX) |
| Backend | 8080 | Java (Spring Boot) |
| Frontend | 5173 | React (Vite) |

Start them in that order. The backend calls the graph service at runtime;
the frontend calls the backend at runtime. Neither calls upstream at startup,
so you can start them in any order, but the graph service should be up before
you exercise the graph endpoints.

## Prerequisites

- **Java 21+** (`java --version`) — Lombok 1.18.46 requires JDK ≤ 24 for
  reliable compilation; JDK 25 works with the version override in `pom.xml`
- **Maven** (use the wrapper: `./mvnw`)
- **Python 3.11+** (`python3 --version`)
- **Node 18+** (`node --version`)

## First-time setup

### Data files

The backend and graph service both read from the same source files:

```
data/raw/airports.dat   (~7,700 airports)
data/raw/airlines.dat   (~6,100 airlines)
data/raw/routes.dat     (~67,700 routes)
```

The graph service also reads a copy at `graph-service/data/routes.dat`.

### Backend

```bash
cd backend
./mvnw clean compile
```

The H2 database is created on first run at `backend/data/flights.mv.db`.
On first startup, three importers run automatically:

- `AirportImportService` → ~7,698 airports
- `AirlineImportService` → ~6,162 airlines
- `RouteImportService` → ~67,663 routes

Each is idempotent: if the table already has rows, it skips.

### Graph service

```bash
cd graph-service
pip install -r requirements.txt
```

On first run, the graph is loaded into memory from `routes.dat`.

### Frontend

```bash
cd frontend
npm install
```

## Daily workflow — starting the stack

### Terminal 1 — Graph service

```bash
cd graph-service
uvicorn app.main:app --reload --port 8000
```

**Note:** the README's example says `uvicorn app.main.py` — that's wrong.
`uvicorn` expects `<module>:<attribute>`, so it's `app.main:app`.

Expected output:

```
INFO:     Uvicorn running on http://127.0.0.1:8000
Loaded graph: 3425 airports, 37595 routes
INFO:     Application startup complete.
```

Note: the graph service deduplicates edges (one per source-destination pair),
so it reports fewer "routes" than the backend's 67,663 — that's expected.

### Terminal 2 — Backend

```bash
cd backend
./mvnw spring-boot:run
```

Expected output (first run):

```
Importing airports from .../data/raw/airports.dat
Airport import completed: 7698 imported, 0 skipped
Importing airlines from .../data/raw/airlines.dat
Airline import completed: 6162 imported, 0 skipped, 1537 invalid IATA dropped, 6077 invalid ICAO dropped
Importing routes from .../data/raw/routes.dat
Route import completed: 67663 imported, 0 skipped
Started BackendApplication
```

On subsequent runs, the importers log "already loaded" and skip.

### Terminal 3 — Frontend

```bash
cd frontend
npm run dev
```

Expected output:

```
  VITE v8.x.x  ready in ~500 ms
  ➜  Local:   http://localhost:5173/
```

### Verify the stack

```bash
./scripts/verify.sh
```

Expected output: all checks pass. If the graph service is down, its checks
fail; if the backend is down, its checks fail; if the frontend is down, it
warns unless you run with `--strict`.

## Verification checklist (manual)

If you want to verify each layer independently:

### 1. Graph service

```bash
curl http://localhost:8000/
# → {"service":"flight graph","status":"running","airports":3425,"routes":37595}

curl http://localhost:8000/connections/AMS
# → {"airport":"AMS","connections":["NDR","TNG",...]}

curl http://localhost:8000/path/AMS/JFK
# → {"from":"AMS","to":"JFK","path":["AMS","JFK"]}
```

### 2. Backend — data

```bash
curl http://localhost:8080/api/airports/AMS
# → {"id":580,"iata":"AMS","icao":"EHAM",...}

curl http://localhost:8080/api/airports/ZZZ
# → 404 (no exception handler yet, so this may be a 500)

curl 'http://localhost:8080/api/hubs?limit=10'
# → top 10 hubs by combined connectivity, e.g. FRA, CDG, AMS, IST, ATL

curl http://localhost:8080/api/airport/AMS/stats
# → {"iata":"AMS","connections":903,"topDestinations":[...],"airlines":[...]}
```

### 3. Backend — graph integration

```bash
curl http://localhost:8080/api/graph/connections/AMS
# → {"airport":"AMS","connections":[...]}

curl http://localhost:8080/api/graph/path/AMS/JFK
# → {"from":"AMS","to":"JFK","path":["AMS","JFK"]}

curl http://localhost:8080/api/routes/compare/AMS/JFK
# → {"routes":[{"id":"AMS-JFK","from":"AMS","to":"JFK","stops":0,...}]}
```

### 4. Frontend — manual

Open http://localhost:5173 and check:

- [ ] Map renders with dark tiles
- [ ] Hub markers appear (~150 visible)
- [ ] Clicking a hub expands its network (more markers appear)
- [ ] Double-clicking a hub selects it as origin
- [ ] The sidebar shows `AirportPanel` with stats
- [ ] The status bar shows "Selected: XXX"
- [ ] Double-clicking a second hub compares routes
- [ ] Route arcs appear on the map, colour-coded
- [ ] `RouteDetails` shows the route list
- [ ] Clicking the map background clears the selection
- [ ] No errors in the browser console

## Running tests

Currently **there are no tests** in the repo beyond the auto-generated
`BackendApplicationTests`. The infrastructure is in place — here's how to add
tests when ready.

### Backend (JUnit 5 + Mockito + AssertJ)

`spring-boot-starter-test` is already a dependency. Add tests under:

```
backend/src/test/java/com/backend/backend/
├── repository/           # @DataJpaTest
├── service/              # unit tests with Mockito
└── controller/           # @WebMvcTest or @SpringBootTest + MockMvc
```

Example — a repository test:

```java
@DataJpaTest
class AirportRepositoryTest {
  @Autowired AirportRepository repo;

  @Test
  void findByIata_returnsAirport() {
    repo.save(airport("AMS", "Amsterdam"));
    assertThat(repo.findByIata("AMS")).isPresent();
  }
}
```

Run: `./mvnw test`

### Graph service (pytest)

Add `pytest` to `requirements.txt` or a `requirements-dev.txt`. Tests go in
`graph-service/tests/`:

```
graph-service/
└── tests/
    ├── test_graph_loader.py
    └── test_endpoints.py
```

Example:

```python
from fastapi.testclient import TestClient
from app.main import app

client = TestClient(app)

def test_root():
    r = client.get("/")
    assert r.status_code == 200
    assert r.json()["status"] == "running"
```

Run: `cd graph-service && pytest`

### Frontend (vitest + Testing Library)

Add dev deps:

```bash
npm install -D vitest @testing-library/react @testing-library/jest-dom jsdom
```

Tests go next to components as `*.test.jsx`.

Run: `npm test`

### Recommended coverage order

If you're adding tests for the first time, prioritise in this order:

1. **Importers** — `AirportImportService`, `AirlineImportService`,
   `RouteImportService`. They're the source of truth for all downstream data,
   and we've already hit two bugs there (column indices, `\N` handling).
2. **`NetworkService`** — the core "expand a network" logic.
3. **`HubService`** — the ranking logic.
4. **`GraphServiceClient`** — the RestClient integration, mockable.
5. **Controller slice tests** — `@WebMvcTest` for each controller.

## Troubleshooting

### Port already in use

```
Port 8080 already in use
```

Find and kill the process:

```bash
lsof -i :8080    # or :8000 or :5173
kill <PID>
```

### H2 database lock

If the backend crashes and leaves `flights.mv.db` locked:

```
Database may be already in use: "Locked by another process"
```

Close any other backend processes and restart. Or:

```bash
rm backend/data/flights.mv.db backend/data/flights.trace.db
./mvnw spring-boot:run
```

This forces a fresh import — takes ~30s.

### Lombok compile errors

If you see:

```
cannot find symbol: method getIata()
```

or

```
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

The Lombok version in `pom.xml` is not compatible with your JDK. Check:

```bash
java --version
./mvnw dependency:tree | grep lombok
```

Lombok must be **1.18.40+** for JDK 25 support. The `pom.xml` overrides the
version in `annotationProcessorPaths`. If both are correct and it still fails,
verify that `annotationProcessorPaths` includes the explicit `<version>` tag —
versionless paths do not pick up the managed version.

### Graph service not loading data

If `/` returns `airports: 0`:

```bash
cd graph-service
ls -la data/routes.dat
```

The default path is `../data/raw/routes.dat` (relative to the working
directory). Run the service from `graph-service/` and confirm the file exists
at `FlightNetworkExplorer/data/raw/routes.dat`.

### `Unsupported upgrade request` in uvicorn log

The JDK `HttpClient` tries an h2c upgrade by default; uvicorn doesn't support
it. We force HTTP/1.1 in `GraphServiceConfig`:

```java
HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1)
```

If you see this warning again, that line may have been removed.

### CORS / network errors in the browser

The frontend calls the backend cross-origin (5173 → 8080). The backend has a
`CorsConfig` that allows the frontend origin. If you see CORS errors in the
browser console, check:

1. `CorsConfig.java` allows `http://localhost:5173`
2. The backend is running on port 8080
3. The request URL in devtools matches what the frontend thinks it's calling

### `routes` table has numeric codes instead of IATA

If `SELECT source_iata FROM routes LIMIT 5` returns numbers instead of 3-letter
codes, the import column indices are wrong. See `RouteImportService.java`:

```java
private static final int COL_SOURCE_IATA = 2;      // not 3
private static final int COL_DESTINATION_IATA = 4; // not 5
```

Delete the DB and re-import to fix.

## Resetting the environment

### Reset the backend database

```bash
cd backend
rm -f data/flights.mv.db data/flights.trace.db
./mvnw spring-boot:run
```

The importers run fresh on next start (~30s).

### Reset the graph service

No persistent state — just restart:

```bash
# Ctrl+C, then
uvicorn app.main:app --reload --port 8000
```

### Reset the frontend

```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

### Full clean

```bash
cd backend && ./mvnw clean
cd ../frontend && rm -rf dist node_modules
cd ../graph-service && find . -name __pycache__ -type d -exec rm -rf {} +
rm -f backend/data/flights.mv.db backend/data/flights.trace.db
```

Then restart the stack.

## Known issues / follow-ups

Documented bugs and pending improvements. Fix these when relevant.

### Backend

- **`AirportStatsService.connections` counts route rows, not distinct
  destinations.** AMS reports 903 (rows) but should report 463 (distinct
  destinations + origins), matching the hubs endpoint. Change
  `COUNT(r)` → `COUNT(DISTINCT r.destinationIata) + COUNT(DISTINCT r.sourceIata)`.
- **`topDestinations` includes self-loops.** AMS appears in its own top
  destinations. Filter `WHERE r.destinationIata <> r.sourceIata`.
- **`Route.distanceKm` is never populated.** The `RouteService.compareRoutes`
  endpoint returns `distanceKm: 0.0` and `estimatedFlightTime: "0h"` for
  every route. Populate during import using Haversine, or compute on the fly
  in `RouteService`.
- **No `@ControllerAdvice`.** Missing airports throw `NoSuchElementException`
  → 500 instead of 404. See `AirportNotFoundException` (already exists) and
  wire a global exception handler.
- **Spring Boot version mismatch.** `pom.xml` is on 3.4.1; the README claims
  4.1.0. Decide which is intended and align both.

### Frontend

- **`index.css` had Vite template styles.** Replaced with a minimal reset in
  Step 3 of the frontend cleanup.
- **`react-router-dom` is a dependency but unused.** Remove from
  `package.json` unless routing is planned.
- **`leaflet-geodesy` and `react-leaflet-cluster`** — verify usage before
  removing.
- **`WorldMap.jsx` was a god component** — split into `useFlightNetwork` hook
  (Step 1) and a renderer.
- **Dblclick to select; single click to expand.** Works but is
  non-obvious. Consider single-click to select, map-click to deselect.
- **`console.log` leftovers** in `WorldMap.jsx:112` and `:145`.

### Graph service

- **Path computation has no timeout.** A dense-graph `/paths` request can
  hang. Add a limit or cache.
- **`neighbours()` returns `[]` for both "no connections" and "unknown
  airport".** A 404 for unknown is more correct.
- **Startup blocks on `graph.load()`.** For the current dataset (~2 MB) this
  is fast enough; consider `asyncio.to_thread` for larger datasets.

### Documentation

- **README's `uvicorn app.main.py` is wrong** — should be `app.main:app`.
  Update the README.

## Repo layout reference

```
FlightNetworkExplorer/
├── backend/            # Spring Boot (Java 21)
│   ├── src/main/java/com/backend/backend/
│   │   ├── client/     # GraphServiceClient (RestClient)
│   │   ├── config/     # CorsConfig, GraphServiceConfig
│   │   ├── controller/ # REST endpoints
│   │   ├── dto/        # response shapes
│   │   ├── exception/  # AirportNotFoundException, import exceptions
│   │   ├── model/      # JPA entities (Airport, Airline, Route)
│   │   ├── repository/ # Spring Data repositories
│   │   └── service/    # business logic + importers
│   └── data/           # H2 database files (gitignored)
├── frontend/           # React + Vite
│   └── src/
│       ├── api/        # axios client
│       ├── components/ # UI
│       ├── hooks/      # useFlightNetwork
│       └── App.jsx
├── graph-service/      # FastAPI + NetworkX
│   ├── app/
│   │   ├── main.py
│   │   └── graph_loader.py
│   └── data/routes.dat
├── data/raw/           # Source CSV data
├── docs/               # This file
└── scripts/            # verify.sh
```

---

*Last updated: as of frontend Step 5.*