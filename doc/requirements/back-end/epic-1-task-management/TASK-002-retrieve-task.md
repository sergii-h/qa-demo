# TASK-002: Retrieve Single Task

**Points:** 2 · **Epic:** Task Management

As a user, I want to retrieve a task by ID so that I can view its details.

## Acceptance Criteria

1. Should return HTTP 200 with complete task when `GET /v1/tasks/{id}` and ID exists
2. Should return HTTP 404 with `"Task not found with id: {taskId}"` when ID does not exist

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should return HTTP 200 with task details when valid ID provided via GET `/v1/tasks/{id}`
   - Should return HTTP 404 when task ID does not exist
3. **Pact**
   - Provider: Should verify provider contract for GET `/v1/tasks/{id}` against consumer pacts
4. **E2E** - Covered by other workflows (view task info, edit task)
5. **Accessibility** - N/A
6. **UAT** - Covered by create-task UAT smoke (GET task in info modal)
7. **Manual** - N/A

