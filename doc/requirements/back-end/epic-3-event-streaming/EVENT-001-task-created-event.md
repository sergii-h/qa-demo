# EVENT-001: Task Created Event

**Points:** 3 · **Epic:** Event Streaming

As a system, I want to publish task-created events to Kafka so downstream systems can react.

## Acceptance Criteria

1. Should publish to `task-event` with `eventType: CREATED` after successful DB save
2. Payload: `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`; key = task ID; no description
3. Should not publish on HTTP 400 or 409

## Test Plan

1. **UT**
   - Should publish nothing when the title already exists
   - Should log the send failure when the Kafka send fails
   - **Note:** An invalid body never reaches the controller, so HTTP 400 has no publisher call to assert and no Kafka absence wait. [TASK-001](../epic-1-task-management/TASK-001-create-task.md) proves the request is rejected. Publishing nothing when the insert fails is that story's unit test. Send failure is covered here once; `produceEvent` is shared with update and delete.
2. **IT**
   - Should publish to `task-event` after a successful create, keyed by task id, with `eventType` `CREATED` and payload `taskId`, `title`, `status`, `priority`, and `timestamp` from the request time, and no description
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

