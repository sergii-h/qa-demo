# VALID-001: External Task Validation

**Points:** 3 · **Epic:** Task Validation

As a user, I want to validate a task against external rules so that I can check data quality.

## Acceptance Criteria

1. Should return HTTP 200 with boolean from `GET /v1/tasks/isValid/{id}`
2. Should return `false` (200) when task ID does not exist — not 404
3. Should call external `POST /external/validate/task` with title, description, status, priority (no id/timestamps)
4. Should return `true` / `false` per external service response

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
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

