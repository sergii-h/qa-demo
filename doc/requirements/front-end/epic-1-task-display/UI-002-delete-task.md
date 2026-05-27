# UI-002: Delete Task

**Points:** 2 · **Epic:** Task Display

As a user, I want to delete a task from the table without a confirmation dialog.

## Acceptance Criteria

1. Should call `DELETE /v1/tasks/{taskId}` on Delete click
2. Should remove row from local state only after successful response (no page refresh)
3. Should keep row on failure (HTTP 500 / network) and allow retry

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should send DELETE request with selected task ID when delete button is clicked
   - Should remove task from local state after successful delete response
   - Should keep task row when delete fails (HTTP 500 or network error)
   - Should allow retry and remove task after subsequent successful delete
   - Should update table display without page refresh
3. **Pact**
   - Should have DELETE `/v1/tasks/{id}` consumer test (HTTP 204)
4. **E2E**
   - Should delete task through UI and verify removal from table
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check — row removal feedback
   - Screen reader announcement on task deletion

