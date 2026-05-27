# UI-003: Create Task Modal

**Points:** 5 · **Epic:** Task Creation

As a user, I want to create a task in a modal with validation and clear errors.

## Acceptance Criteria

1. Should open "New task" modal from Create button with fields: title (required), description (optional), status (default TODO), priority (default MEDIUM)
2. Should offer status TODO / IN_PROGRESS / DONE and priority LOW / MEDIUM / HIGH
3. Should disable Create when title empty; validate before submit
4. Should show errors: `"Title is required"`, `"Title must not exceed 100 characters"`, `"Task with this title already exists"`, `"Failed to create task. Please try again."` (generic); clear title error on typing
5. Should show loading spinner during POST; close and refresh table on success; Close discards without save

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should create task with all values and refresh table
   - Should create task with required values and refresh table
   - Should allow successful creation after invalid title is corrected
   - Should not create a task when modal is closed without saving, and should reset form on reopen
   - Should display generic error for POST API failures (HTTP 400/500)
   - Should allow retry and create task after initial POST failure
   - Should keep create flow available when initial GET fails (HTTP 500 or network rejection)
   - Should close modal when refresh GET fails after successful POST (HTTP 500 or network rejection)
   - Should display generic error when POST request is rejected (network failure)
3. **Pact**
   - Should have POST `/v1/tasks` consumer test (HTTP 201 with task response)
   - Should have POST `/v1/tasks` consumer test (HTTP 409 duplicate title)
4. **E2E**
   - Should create task through complete UI workflow
5. **Accessibility**
   - axe-core checks on create modal in E2E
6. **UAT**
   - Should create task
7. **Manual**
   - Visual check
   - Focus trap within modal
   - Screen reader announcements for validation errors

## Notes

- Modal ID `create-task-modal`; test IDs: `create-task-title-input`, `create-button`, `close-button`, `title-error`, `loading-spinner`
