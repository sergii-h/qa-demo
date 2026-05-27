# EVENT-003: Task Deleted Event

**Points:** 3 · **Epic:** Event Streaming

As a system, I want to publish task-deleted events to Kafka so downstream systems can react.

## Acceptance Criteria

1. Should publish to `task-event` with `eventType: DELETED` after successful DB delete (pre-delete values)
2. Payload: `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`; key = task ID
3. Should not publish on HTTP 404

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should publish event to Kafka topic `task-event` when task deleted
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

