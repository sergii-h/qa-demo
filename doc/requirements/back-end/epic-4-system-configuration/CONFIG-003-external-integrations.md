# CONFIG-003: External Service Integration

**Points:** 3 · **Epic:** System Configuration

As a system, I want MongoDB, Kafka, and the external validation API configured so the app can run end-to-end.

## Acceptance Criteria

1. **MongoDB** — Connect at startup; persist to `tasks` collection
2. **Kafka** — Producer at startup; JSON value + String key serializers; publish to configured topic
3. **External API** — WebClient base URL from config; used for validation calls

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should connect to MongoDB at configured URI
   - Should persist data to "tasks" collection
   - Should initialize Kafka producer with configured bootstrap servers
   - Should send events with JSON serialization to configured topic
   - Should create WebClient with configured base URL for external service
3. **Pact** - N/A
4. **E2E** - Implicitly tested (application functions correctly)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

## Notes

- Defaults: `mongodb://localhost:27018/task_db`, `localhost:9094`, topic `task-event`, external `http://localhost:8085`
