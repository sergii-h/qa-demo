# Performance Tests (k6)

Load and spike tests for the Task Management API using [k6](https://k6.io/).

## Prerequisites

- [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/) runtime installed locally (`brew install k6` on macOS)
- npm dependencies installed for IDE types:

```bash
cd performance && npm install
```

- Application stack running (MongoDB, Kafka, WireMock, demo-service):

```bash
docker compose -f docker/docker-compose/run-application.yml up -d
```

Verify the API is up: http://localhost:8080/v1/tasks

## Scenarios

| Script | Endpoint | Purpose |
|--------|----------|---------|
| `baseline-load.js` | GET `/v1/tasks` | Steady read load — simulates table refresh on every page open |
| `create-under-load.js` | POST `/v1/tasks` | Sustained writes — Kafka event + persistence under load |
| `concurrent-uniqueness.js` | POST `/v1/tasks` × N | Race on the same title — expects exactly one 201 and N−1 409 responses |
| `spike-test.js` | GET `/v1/tasks` | Traffic burst then recovery |

## Running

From the `performance/` directory (after `npm install`):

```bash
# Baseline read load (10 VUs, ~3 min)
npm run test:baseline

# Create under load (5 VUs, ~3 min)
npm run test:create

# Concurrent uniqueness (50 simultaneous POSTs with the same title)
npm run test:uniqueness

# Spike test (10 → 200 → 10 VUs)
npm run test:spike
```

Or invoke k6 directly from the project root:

```bash
k6 run performance/baseline-load.js
```

### Environment variables

| Variable | Default | Description |
|----------|---------|-------------|
| `BASE_URL` | `http://localhost:8080` | API base URL (without `/v1`) |
| `CONCURRENT_REQUESTS` | `50` | Number of simultaneous POSTs in the uniqueness race |
| `RACE_TITLE` | auto-generated | Fixed title for the concurrent uniqueness test |

Example against a remote environment:

```bash
BASE_URL=https://staging.example.com k6 run performance/baseline-load.js
```

## Thresholds

Default pass/fail thresholds are defined in each script and shared config:

- **Read scenarios:** p95 latency < 500 ms, error rate < 1%
- **Write scenario:** p95 latency < 2 s, error rate < 1%
- **Concurrent uniqueness:** exactly 1× HTTP 201, (N−1)× HTTP 409, no other statuses
- **Spike test:** p95 latency < 2 s during recovery (relaxed vs baseline)

Tune VU counts and stage durations in each script's `options.scenarios` block to match your target environment.

## Notes

- Create tests accumulate tasks in MongoDB; reset the database or use a dedicated perf environment for repeatable runs.
- **Concurrent uniqueness** requires a unique index on `title` (enforced by demo-service). Restart the service after deploy so MongoDB creates the index. If duplicates already exist from earlier runs, drop the `tasks` collection before re-testing.
- The concurrent uniqueness test treats HTTP 409 as an expected outcome (not a k6 failure) via `http.expectedStatuses(201, 409)`.
- POST `/v1/tasks` publishes a Kafka event; ensure Kafka is healthy before running write scenarios.
