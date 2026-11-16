# CONFIG-003: External Service Integration

**Points:** 3 · **Epic:** System Configuration

As a system, I want MongoDB, Kafka, and the external validation API configured so the app can run end-to-end.

## Acceptance Criteria

1. **MongoDB** — Connect at startup; persist to `tasks` collection
2. **Kafka** — Producer at startup; JSON value + String key serializers; publish to configured topic
3. **External API** — WebClient base URL from config; used for validation calls

## Test Plan

1. **UT** - N/A. MongoDB, Kafka, and WebClient startup wiring has no branching logic
2. **IT** - Covered by the public-boundary tests that use this wiring; do not add a configuration suite
   - MongoDB connection and the `tasks` collection: create then read in [TASK-001](../epic-1-task-management/TASK-001-create-task.md)
   - Kafka producer, JSON values, and the configured topic: [EVENT-001](../epic-3-event-streaming/EVENT-001-task-created-event.md), [EVENT-002](../epic-3-event-streaming/EVENT-002-task-updated-event.md), [EVENT-003](../epic-3-event-streaming/EVENT-003-task-deleted-event.md)
   - WebClient base URL used for validation: [VALID-001](../epic-2-task-validation/VALID-001-external-validation.md)
3. **Pact** - N/A
4. **E2E** - Implicitly tested (application functions correctly)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

## Notes

- Defaults: `mongodb://localhost:27018/task_db`, `localhost:9094`, topic `task-event`, external `http://localhost:8085`
