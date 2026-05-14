# VALID-001: External Task Validation

**Epic:** Task Validation  
**Priority:** Medium  
**Story Points:** 3

## Description
As a user, I want to validate a task against external business rules so that I can ensure task data meets external system requirements.

## Acceptance Criteria

1. Should return HTTP 200 with boolean validation result from external service when valid task ID exists

2. Should return HTTP 200 with `false` when task ID does not exist

3. Should call external validation service with task title, description, status, and priority when task exists in database

4. Should return `true` when external service validates task successfully

5. Should return `false` when external service validation fails

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should return HTTP 200 with `true` when task exists and is valid via GET `/v1/tasks/isValid/{id}`
   - Should return not valid validation result when not valid description
   - Should return HTTP 200 with `false` when task ID does not exist
   - Should call external validation service with task data
3. **Pact**
   - Should verify provider contract for GET `/v1/tasks/isValid/{id}` (HTTP 200 with validation result)
4. **E2E**
   - Should display validation status in task info modal
5. **UAT** - N/A

## Technical Notes
- External validation endpoint: POST `/external/validate/task`
- Uses reactive WebClient for external service communication
- Task ID and timestamps are not included in validation request
- External service URL configured via `external.service.url` property

