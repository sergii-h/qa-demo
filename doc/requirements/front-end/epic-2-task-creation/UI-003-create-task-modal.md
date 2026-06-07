# UI-003: Create Task

**Points:** 5 · **Epic:** Task Creation

As a user, I want to create a task in a dedicated flow with validation and clear errors.

## Acceptance Criteria

1. Should open create flow from the create entry point with fields: title (required), description (optional), status (default TODO), priority (default MEDIUM)
2. Should offer status TODO / IN_PROGRESS / DONE and priority LOW / MEDIUM / HIGH
3. Should disable submit when title is empty; validate before submit
4. Should show errors: `"Title must not exceed 100 characters"`, `"Task with this title already exists"`, `"Failed to create task. Please try again."` (generic); clear title error on typing
5. Should show loading spinner during POST; close create flow and refresh list on success; closing without save discards changes

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should create task with all values and refresh list
   - Should create task with required values and refresh list
   - Should allow successful creation after invalid title is corrected
   - Should not create a task when create flow is closed without saving, and should reset form on reopen
   - Should display generic error for POST API failures (HTTP 400/500)
   - Should allow retry and create task after initial POST failure
   - Should keep create flow available when initial GET fails (HTTP 500 or network rejection)
   - Should close create flow when refresh GET fails after successful POST (HTTP 500 or network rejection)
   - Should display generic error when POST request is rejected (network failure)
   - Should have translations for create flow
3. **Pact**
   - Should have POST `/v1/tasks` consumer test (HTTP 201 with task response)
   - Should have POST `/v1/tasks` consumer test (HTTP 409 duplicate title)
4. **E2E**
   - Should create task through complete UI workflow
5. **Accessibility**
   - axe-core checks on create flow in E2E
6. **UAT**
   - Should create task
7. **Manual**
   - Visual check
   - Focus management within create flow
   - Screen reader announcements for validation errors
