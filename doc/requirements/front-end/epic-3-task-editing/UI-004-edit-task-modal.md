# UI-004: Edit Task Modal

**Points:** 5 · **Epic:** Task Editing

As a user, I want to edit a task in a pre-populated modal with the same validation as create.

## Acceptance Criteria

1. Should open "Edit {title}" modal with spinner, then pre-filled title, description, status, priority (same options as [UI-003](../epic-2-task-creation/UI-003-create-task-modal.md))
2. Should disable Save when title empty; same client validation and error messages as create (with `"Failed to update task..."` for generic errors)
3. Should fetch via `GET /v1/tasks/{id}`, save via `PUT /v1/tasks/{id}`; spinner during load/update
4. Should close and refresh table on success; Close discards without save

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should update task with modified values and refresh tasks table
   - Should update task with removed description and refresh tasks table
   - Should close edit modal without saving changes
   - Should proceed with save after user corrects invalid title
   - Should keep edit flow available when initial GET fails (HTTP 500 or network rejection)
   - Should display generic error for PUT API failures (HTTP 400/500)
   - Should allow retry and save after initial PUT failure
   - Should display generic error when PUT request is rejected (network failure)
   - Should have translations for edit task modal
3. **Pact**
   - Should have GET `/v1/tasks/{id}` consumer test (HTTP 200)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 200 with updated task)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 409 duplicate title)
4. **E2E**
   - Should edit task through complete UI workflow
5. **Accessibility**
   - axe-core checks on edit modal in E2E
6. **UAT** - N/A
7. **Manual**
   - Visual check
   - Focus trap within modal
   - Screen reader announcements for validation errors

## Notes

- Modal ID `edit-task-modal`; test IDs: `save-button`, `close-button`
