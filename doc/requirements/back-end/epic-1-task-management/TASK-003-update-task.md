# TASK-003: Update Task

**Epic:** Task Management  
**Priority:** High  
**Story Points:** 5

## Description
As a user, I want to update an existing task's title, description, status, and priority so that I can keep task information current.

## Acceptance Criteria

1. Should return HTTP 200 with updated task details when valid task ID and valid update data provided

2. Should return HTTP 404 with error message "Task not found with id: {taskId}" when task ID does not exist

3. Should return HTTP 409 with error message "Task with title '{title}' already exists" when update contains new title that already exists on different task

4. Should allow update without duplicate title validation when update contains same title as current task

5. Should return HTTP 400 with validation errors when title is blank or fields exceed length limits

6. Should set `updatedDate` to current timestamp and preserve `createdDate` when task successfully updated

7. Should publish task updated event to Kafka topic when task successfully updated

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
5. **UAT** - N/A

## Technical Notes
- All validations from TASK-001 apply
- Task ID and createdDate are immutable

