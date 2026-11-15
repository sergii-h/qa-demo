# VALID-001: External Task Validation

**Points:** 3 · **Epic:** Task Validation

As a user, I want to validate a task against external rules so that I can check data quality.

## Acceptance Criteria

1. Should return HTTP 200 with boolean from `GET /v1/tasks/isValid/{id}`
2. Should return `false` (200) when task ID does not exist — not 404
3. Should call external `POST /external/validate/task` with title, description, status, priority (no id/timestamps)
4. Should return `true` / `false` per external service response
5. Should return HTTP 500 when the external service responds with a server error

## Test Plan

1. **UT** - N/A. The outbound POST is reachable through GET `/v1/tasks/isValid/{id}`; the integration scenario asserts the uri, content type, and body
2. **IT**
   - Should return HTTP 200 with `true` when the task exists and the external service accepts it, via GET `/v1/tasks/isValid/{id}`
   - Should return HTTP 200 with `false` when the external service rejects the task
   - Should return HTTP 200 with `false` when the task id does not exist, and not call the external service
   - Should POST `/external/validate/task` with `Content-Type: application/json` and a body of title, description, status, and priority only
   - Should return HTTP 500 when the external service responds with a server error
3. **Pact**
   - Should verify provider contract for GET `/v1/tasks/isValid/{id}` (HTTP 200 with validation result)
4. **E2E**
   - Should display validation status in task info modal
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

