# UI-005: Info Task Modal

**Epic:** Task Information  
**Priority:** Medium  
**Story Points:** 3

## Description
As a user, I want to view detailed information about a task including all its properties and validation status so that I can understand the complete task context.

## Acceptance Criteria

1. Should open modal dialog with task title as header when user clicks Info button on a task row

2. Should display loading spinner while task data is loading after info modal opens

3. Should show all task information in read-only format after task data is loaded

4. Should display following task information:
   - Description (or "No description" if empty)
   - Status (as colored tag)
   - Priority (as colored tag)
   - Created date (formatted as locale string)
   - Last Updated date (formatted as locale string)
   - Validation status (green check or red X icon)

5. Should use same status tag colors as table:
   - TODO → Blue/Info
   - IN_PROGRESS → Orange/Warning
   - DONE → Green/Success

6. Should use same priority tag colors as table:
   - LOW → Green/Success
   - MEDIUM → Orange/Warning
   - HIGH → Red/Danger

7. Should fetch validation status from external service and display result

8. Should show green check icon (pi-check) when task is validated

9. Should show red X icon (pi-times) when task is not validated

10. Should close modal when user clicks outside or on close button

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should open info modal and display task details for all-values and required-values datasets
   - Should show both validation outcomes (valid and not valid indicators)
   - Should close info modal on close action
   - Should keep info flow available when task details request fails (HTTP 500 or network rejection)
   - Should keep info flow available when validation request fails (HTTP 500 or network rejection)
3. **Pact**
   - Should have GET `/v1/tasks/{id}` consumer test (HTTP 200)
   - Should have GET `/v1/tasks/isValid/{id}` consumer test (HTTP 200 with boolean)
4. **E2E**
   - Should view task info through UI
5. **Accessibility**
   - Should have proper ARIA labels for read-only content
   - Should trap focus within modal
6. **UAT** - N/A

## Technical Notes
- Uses PrimeReact Dialog component (minimum width: 480px)
- Fetches task data via `getTask(taskId)`
- Fetches validation status via `getIsValid(taskId)`
- Handles task/validation fetch failures with safe fallbacks so modal remains usable
- Displays status/priority using PrimeReact Tag component
- Date formatting uses `toLocaleString()`
- Modal ID: `info-task-modal`
- Test IDs: `valid`, `notValid`
- Element IDs: `description`, `status`, `priority`, `createdDate`, `updatedDate`, `valid`

