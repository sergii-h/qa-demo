# TASK-003: Update Task

**Points:** 5 · **Epic:** Task Management

As a user, I want to update a task's fields so that I can keep task information current.

## Acceptance Criteria

1. Should return HTTP 200 with updated task when `PUT /v1/tasks/{id}` with valid data
2. Should return HTTP 404 with `"Task not found with id: {taskId}"` when ID does not exist
3. Should return HTTP 409 with `"Task with title '{title}' already exists"` when new title belongs to another task
4. Should allow same title as current task without 409
5. Should return HTTP 400 when title blank or fields exceed length limits ([TASK-001](TASK-001-create-task.md) rules)
6. Should set `updatedDate` to now and preserve `createdDate` on success
7. Should publish task-updated event to Kafka on success

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should successfully update task with valid data via PUT `/v1/tasks/{id}` → HTTP 200
   - Should return HTTP 404 when task ID does not exist
   - Should return HTTP 409 when new title conflicts with existing task
   - Should allow update when title remains the same
   - Should return HTTP 400 for validation errors
   - **Note:** Kafka event publishing is tested in EVENT-002 integration tests
3. **Pact**
   - Should verify provider contract for PUT `/v1/tasks/{id}` (HTTP 200 with updated task)
   - Should verify provider contract for PUT `/v1/tasks/{id}` (HTTP 409 duplicate title)
4. **E2E**
   - Should update task through complete UI workflow (edit modal)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

## Notes

- `id` and `createdDate` are immutable
