# Epic 3: Event Streaming

Kafka events for task created, updated, and deleted — published only after successful DB operations.

## User Stories

- [EVENT-001](EVENT-001-task-created-event.md) — Task Created Event (3 pts)
- [EVENT-002](EVENT-002-task-updated-event.md) — Task Updated Event (3 pts)
- [EVENT-003](EVENT-003-task-deleted-event.md) — Task Deleted Event (3 pts)

**Total:** 9 pts

## Epic scope

- Topic: `task-event`; message key = task ID
- Payload: `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType` (CREATED \| UPDATED \| DELETED)
- No events on validation failures, not-found, or duplicate-title responses
