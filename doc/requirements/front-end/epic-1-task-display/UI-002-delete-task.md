# UI-002: Delete Task

**Epic:** Task Display  
**Priority:** High  
**Story Points:** 2

## Description
As a user, I want to delete a task from the table so that I can remove tasks I no longer need.

## Acceptance Criteria

1. Should remove task from the table without page refresh after successful delete operation from a task row

2. Should send DELETE request to backend API `/v1/tasks/{taskId}`

3. Should remove task from local state after successful deletion

4. Should update table display to reflect removal

5. Should not require confirmation dialog (immediate deletion)

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
5. **Accessibility**
   - Should announce task deletion to screen readers
6. **UAT** - N/A

## Technical Notes
- Calls `deleteTask(taskId)` service function
- Filters local state to remove deleted task only after successful delete response
- Keeps current rows unchanged on delete failure so user can retry
- Delete button styled with `p-button-outlined p-button-danger` classes

