# TASK-004: Delete Task

**Epic:** Task Management  
**Priority:** Medium  
**Story Points:** 3

## Description
As a user, I want to delete a task so that I can remove tasks that are no longer needed.

## Acceptance Criteria

1. Should return HTTP 204 with no content when valid task ID deleted successfully

2. Should return HTTP 404 with error message "Task not found with id: {taskId}" when task ID does not exist

3. Should remove task from database when deletion successful

4. Should publish task deleted event to Kafka topic when task successfully deleted

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
5. **UAT** - N/A

## Technical Notes
- Deletion is permanent (hard delete)
- Event includes task details before deletion

