# TASK-004: Delete Task

**Points:** 3 · **Epic:** Task Management

As a user, I want to delete a task so that I can remove work items I no longer need.

## Acceptance Criteria

1. Should return HTTP 204 when `DELETE /v1/tasks/{id}` and task exists (hard delete from DB)
2. Should return HTTP 404 with `"Task not found with id: {taskId}"` when ID does not exist
3. Should publish task-deleted event to Kafka on successful delete (payload from pre-delete state)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should successfully delete task via DELETE `/v1/tasks/{id}` → HTTP 204
   - Should return HTTP 404 when task ID does not exist
   - **Note:** Kafka event publishing is tested in EVENT-003 integration tests
3. **Pact**
   - Should verify provider contract for DELETE `/v1/tasks/{id}` (HTTP 204 success)
4. **E2E**
   - Should delete task through UI (table delete button)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

