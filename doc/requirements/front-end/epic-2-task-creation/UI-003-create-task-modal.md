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
   - Should create task with all values, send correct POST request and add new task to the list after successful response
   - Should create task with required values, send correct POST request and add new task to the list after successful response
   - Should allow successful creation after invalid title is corrected
   - Should not create a task when create form is closed without saving, and should reset form on reopen
   - Should allow retry and create task after initial POST failure
   - Should allow opening create form when initial GET tasks fails with HTTP 500
   - Should close create form when refresh GET fails with HTTP 500 after successful POST
   - Should display generic error on create form when POST request is rejected with HTTP 500
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
