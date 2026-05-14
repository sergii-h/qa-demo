# TASK-001: Create Task

**Epic:** Task Management  
**Priority:** High  
**Story Points:** 5

## Description
As a user, I want to create a new task with title, description, status, and priority so that I can track work items.

## Acceptance Criteria

1. Should create task with HTTP 201 and return it with generated ID and timestamps when valid data with all required fields provided

2. Should return HTTP 409 with error message "Task with title '{title}' already exists" when task request has duplicate title

3. Should return HTTP 400 with error "Title is required" when title is blank

4. Should return HTTP 400 with error "Title must not exceed 100 characters" when title exceeds 100 characters

5. Should return HTTP 400 with error "Description must not exceed 500 characters" when description exceeds 500 characters

6. Should return HTTP 400 with error "Status is required" when status is missing

7. Should return HTTP 400 with error "Priority is required" when priority is missing

8. Should publish task created event to Kafka topic when task successfully created

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
5. **UAT** - N/A

## Technical Notes
- Task ID is auto-generated as MongoDB ObjectId
- Both `createdDate` and `updatedDate` are set to current timestamp
- Status values: TODO, IN_PROGRESS, DONE
- Priority values: LOW, MEDIUM, HIGH

