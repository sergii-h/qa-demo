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

1. **UT**
   - Should publish nothing when saving the task fails
2. **IT**
   - Should return HTTP 200 with the updated fields, `createdDate` preserved, and `updatedDate` set to the request time, when valid data is put to PUT `/v1/tasks/{id}`
   - Should return HTTP 404 with `"Task not found with id: {taskId}"` when the id does not exist
   - Should return HTTP 409 with `"Task with title '{title}' already exists"` when the new title belongs to another task, and leave both tasks unchanged
   - Should return HTTP 200 with the updated task when the title is unchanged
   - Should return HTTP 400 and leave the stored task unchanged when the body violates the [TASK-001](TASK-001-create-task.md) validation rules
   - **Note:** The updated event is [EVENT-002](../epic-3-event-streaming/EVENT-002-task-updated-event.md). Publishing nothing on 404 or 409 is that story's unit test.
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
