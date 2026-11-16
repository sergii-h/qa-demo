# CONFIG-002: Error Handling

**Points:** 3 · **Epic:** System Configuration

As an API consumer, I want consistent error responses so I can handle failures predictably.

## Acceptance Criteria

1. Should return HTTP 400 with `{fieldName: message}` map for validation errors (all invalid fields in one response)
2. Should return HTTP 404 with `{message: "..."}` for `TaskNotFoundException`
3. Should return HTTP 409 with `{message: "..."}` for duplicate title / duplicate key
4. Should apply handlers globally via `@ControllerAdvice` (`MethodArgumentNotValidException`, `TaskNotFoundException`, etc.)

## Test Plan

1. **UT**
   - Should return HTTP 409 with `"Task with this title already exists"` when a duplicate key is raised. The title check handles the usual conflict; this race is not provoked through HTTP
2. **IT**
   - Should return HTTP 400 with every invalid field in one `{fieldName: message}` map
   - **Note:** HTTP 404 with `{message}` is [TASK-002](../epic-1-task-management/TASK-002-retrieve-task.md), [TASK-003](../epic-1-task-management/TASK-003-update-task.md), and [TASK-004](../epic-1-task-management/TASK-004-delete-task.md). HTTP 409 for a duplicate title is [TASK-001](../epic-1-task-management/TASK-001-create-task.md) and [TASK-003](../epic-1-task-management/TASK-003-update-task.md). Those responses are what prove `@ControllerAdvice` is registered. The individual validation messages are TASK-001.
3. **Pact** - N/A for 400/404 (IT only). Duplicate-title 409 contracts covered in [TASK-001](../epic-1-task-management/TASK-001-create-task.md) / [TASK-003](../epic-1-task-management/TASK-003-update-task.md) and FE consumer tests
4. **E2E** - N/A. The backend tests generate the message, Pact locks the 409 contract, and the frontend Error Handling tests show it in the create and edit modals
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

