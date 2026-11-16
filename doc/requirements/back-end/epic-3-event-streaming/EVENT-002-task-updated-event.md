# EVENT-002: Task Updated Event

**Points:** 3 · **Epic:** Event Streaming

As a system, I want to publish task-updated events to Kafka so downstream systems can react.

## Acceptance Criteria

1. Should publish to `task-event` with `eventType: UPDATED` after successful DB save (updated field values)
2. Payload: `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`; key = task ID
3. Should not publish on HTTP 400, 404, or 409

## Test Plan

1. **UT**
   - Should publish nothing when the task id does not exist
   - Should publish nothing when the new title belongs to another task
   - **Note:** HTTP 400 never reaches the controller (see [EVENT-001](EVENT-001-task-created-event.md)). Kafka send failure is the shared `produceEvent` path, covered once in EVENT-001. Publishing nothing when the save fails is [TASK-003](../epic-1-task-management/TASK-003-update-task.md).
2. **IT**
   - Should publish to `task-event` after a successful update, keyed by task id, with `eventType` `UPDATED` and the updated `taskId`, `title`, `status`, `priority`, and `timestamp` from the request time
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

