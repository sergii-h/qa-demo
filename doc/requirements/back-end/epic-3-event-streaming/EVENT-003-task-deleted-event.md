# EVENT-003: Task Deleted Event

**Points:** 3 · **Epic:** Event Streaming

As a system, I want to publish task-deleted events to Kafka so downstream systems can react.

## Acceptance Criteria

1. Should publish to `task-event` with `eventType: DELETED` after successful DB delete (pre-delete values)
2. Payload: `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`; key = task ID
3. Should not publish on HTTP 404

## Test Plan

1. **UT**
   - Should publish nothing when the task id does not exist
   - **Note:** Kafka send failure is covered once in [EVENT-001](EVENT-001-task-created-event.md). Publishing nothing when the delete fails is [TASK-004](../epic-1-task-management/TASK-004-delete-task.md).
2. **IT**
   - Should publish to `task-event` after a successful delete, keyed by task id, with `eventType` `DELETED` and the pre-delete `taskId`, `title`, `status`, `priority`, and `timestamp`
3. **Pact** - N/A
4. **E2E** - N/A (event streaming not visible in UI)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

