# UI-004: Edit Task

**Points:** 5 · **Epic:** Task Editing

As a user, I want to edit a task in a pre-populated flow with the same validation as create.

## Acceptance Criteria

1. Should open edit flow titled with task name, with spinner, then pre-filled title, description, status, priority (same options as [UI-003](../epic-2-task-creation/UI-003-create-task-modal.md))
2. Should disable Save when title is empty; same client validation and error messages as create (with `"Failed to update task..."` for generic errors)
3. Should fetch via `GET /v1/tasks/{id}`, save via `PUT /v1/tasks/{id}`; spinner during load/update
4. Should close edit flow and refresh list on success; closing without save discards changes

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should update task with modified values, send correct PUT request and show modified task title in the list after successful response
   - Should update task with removed description
   - Should not modify task when edit form is closed without saving
   - Should proceed with save after user corrects invalid title
   - Should allow opening edit form when initial GET tasks fails with HTTP500/network-error
   - Should close edit form when refresh GET fails with HTTP500/network-error after successful PUT
   - Should allow retry and save task after initial PUT failure
   - Should display generic error on edit form when PUT request is rejected with HTTP400/HTTP500/network-error
   - Should have translations for edit flow
3. **Pact**
   - Should have GET `/v1/tasks/{id}` consumer test (HTTP 200)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 200 with updated task)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 409 duplicate title)
4. **E2E**
   - Should edit task through complete UI workflow
5. **Accessibility**
   - axe-core checks on edit flow in E2E
6. **UAT** - N/A
7. **Manual**
   - Visual check
   - Focus management within edit flow
   - Screen reader announcements for validation errors
