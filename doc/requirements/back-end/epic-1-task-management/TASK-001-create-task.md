# TASK-001: Create Task

**Points:** 5 · **Epic:** Task Management

As a user, I want to create a task with title, description, status, and priority so that I can track work items.

## Acceptance Criteria

1. Should return HTTP 201 with generated `id`, `createdDate`, and `updatedDate` when valid data is posted to `POST /v1/tasks`
2. Should return HTTP 409 with `"Task with title '{title}' already exists"` when title is duplicate
3. Should return HTTP 400 with field errors:
   - `"Title is required"` (blank title)
   - `"Title must not exceed 100 characters"`
   - `"Description must not exceed 500 characters"`
   - `"Status is required"` / `"Priority is required"` (missing fields)
4. Should publish task-created event to Kafka on successful create

## Test Plan

1. **UT**
   - Should publish nothing when inserting the task fails
2. **IT**
   - Should return HTTP 201 with a generated `id` and `createdDate` equal to `updatedDate` when valid data is posted to POST `/v1/tasks`
   - Should return HTTP 409 with `"Task with title '{title}' already exists"` when the title already exists, and leave the existing task unchanged
   - Should return HTTP 400 with the field-error map for a blank title (`"Title is required"`), a title over 100 characters (`"Title must not exceed 100 characters"`), a description over 500 characters (`"Description must not exceed 500 characters"`), a missing status (`"Status is required"`), and a missing priority (`"Priority is required"`)
   - **Note:** The created event is [EVENT-001](../epic-3-event-streaming/EVENT-001-task-created-event.md). Publishing nothing on 409 is that story's unit test. Several invalid fields in one response are [CONFIG-002](../epic-4-system-configuration/CONFIG-002-error-handling.md).
3. **Pact**
   - Should verify provider contract for POST `/v1/tasks` (HTTP 201 with created task)
   - Should verify provider contract for POST `/v1/tasks` (HTTP 409 duplicate title)
4. **E2E**
   - Should create task through complete UI workflow (critical path)
5. **Accessibility** - N/A
6. **UAT** - Covered by create-task UAT smoke (real POST against full stack)
7. **Manual** - N/A

## Notes

- ID: MongoDB ObjectId; status TODO \| IN_PROGRESS \| DONE; priority LOW \| MEDIUM \| HIGH
