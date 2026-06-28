# UI-002: Delete Task

**Points:** 2 · **Epic:** Task Display

As a user, I want to delete a task from the list without a confirmation dialog.

## Acceptance Criteria

1. Should call `DELETE /v1/tasks/{taskId}` when Delete is activated
2. Should remove the task from the list only after a successful response (no full client reload)
3. Should keep the task in the list on failure (HTTP 500 / network) and allow retry

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should send delete request with selected task ID when delete is triggered and remove task from list after successful delete response
   - Should keep task in list when delete fails with HTTP500/network-error
   - Should allow delete retry after failure and remove task when retry succeeds
3. **Pact**
   - Should have DELETE `/v1/tasks/{id}` consumer test (HTTP 204)
4. **E2E**
   - Should delete task through UI and verify removal from list
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check — removal feedback
   - Screen reader announcement on task deletion
