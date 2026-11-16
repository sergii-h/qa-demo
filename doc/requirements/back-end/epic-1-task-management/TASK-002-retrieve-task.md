# TASK-002: Retrieve Single Task

**Points:** 2 · **Epic:** Task Management

As a user, I want to retrieve a task by ID so that I can view its details.

## Acceptance Criteria

1. Should return HTTP 200 with complete task when `GET /v1/tasks/{id}` and ID exists
2. Should return HTTP 404 with `"Task not found with id: {taskId}"` when ID does not exist

## Test Plan

1. **UT** - N/A. Retrieval is a pass-through read; both acceptance criteria are covered by the integration scenarios
2. **IT**
   - Should return HTTP 200 with the task when a valid id is provided via GET `/v1/tasks/{id}`
   - Should return HTTP 404 with `"Task not found with id: {taskId}"` when the id does not exist
3. **Pact**
   - Provider: Should verify provider contract for GET `/v1/tasks/{id}` against consumer pacts
4. **E2E** - Covered by other workflows (view task info, edit task)
5. **Accessibility** - N/A
6. **UAT** - Covered by create-task UAT smoke (GET task in info modal)
7. **Manual** - N/A

