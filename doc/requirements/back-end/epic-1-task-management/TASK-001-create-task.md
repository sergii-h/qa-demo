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

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should successfully create task with valid data via POST `/v1/tasks` → HTTP 201
   - Should return HTTP 409 when duplicate title exists
   - Should return HTTP 400 for all validation errors (title blank, too long, missing required fields)
   - **Note:** Kafka event publishing is tested in EVENT-001 integration tests
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
