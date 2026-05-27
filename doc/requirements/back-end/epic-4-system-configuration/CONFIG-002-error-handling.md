# CONFIG-002: Error Handling

**Points:** 3 · **Epic:** System Configuration

As an API consumer, I want consistent error responses so I can handle failures predictably.

## Acceptance Criteria

1. Should return HTTP 400 with `{fieldName: message}` map for validation errors (all invalid fields in one response)
2. Should return HTTP 404 with `{message: "..."}` for `TaskNotFoundException`
3. Should return HTTP 409 with `{message: "..."}` for duplicate title / duplicate key
4. Should apply handlers globally via `@ControllerAdvice` (`MethodArgumentNotValidException`, `TaskNotFoundException`, etc.)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should return HTTP 400 with field errors for validation failures
   - Should return HTTP 404 with message for `TaskNotFoundException`
   - Should return HTTP 409 with message for duplicate title
   - Should handle multiple validation errors in single response
   - Should format error responses consistently
3. **Pact** - N/A for 400/404 (IT only). Duplicate-title 409 contracts covered in [TASK-001](../epic-1-task-management/TASK-001-create-task.md) / [TASK-003](../epic-1-task-management/TASK-003-update-task.md) and FE consumer tests
4. **E2E** - Error messages displayed in UI (create/edit modals)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual** - N/A

