# UI-004: Edit Task Modal

**Epic:** Task Editing  
**Priority:** High  
**Story Points:** 5

## Description
As a user, I want to edit an existing task through a modal dialog so that I can update task information as my work progresses.

## Acceptance Criteria

1. Should open modal dialog with title "Edit {task title}" when user clicks Edit button on a task row

2. Should display loading spinner while task data is loading after edit modal opens

3. Should pre-populate all form fields with current task data after task data is loaded

4. Should display form with following fields:
   - Title (text input, required, pre-filled)
   - Description (textarea, optional, pre-filled)
   - Status (dropdown, required, pre-selected)
   - Priority (dropdown, required, pre-selected)

5. Should provide same dropdown options as create modal

6. Should close modal and refresh tasks table with updated task after successful save

7. Should validate title is not empty before allowing submission

8. Should disable Save button when title is empty

9. Should display error message "Title is required" when title is blank

10. Should display error message "Title must not exceed 100 characters" when title exceeds limit

11. Should display error "Task with this title already exists" when backend returns duplicate title error

12. Should display error message "Failed to update task. Please try again." for general errors

13. Should clear title error when user starts typing after error

14. Should show loading spinner during task update

15. Should have Close and Save buttons in footer

16. Should close modal when Close button clicked without saving

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
3. **Pact**
   - Should have GET `/v1/tasks/{id}` consumer test (HTTP 200)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 200 with updated task)
   - Should have PUT `/v1/tasks/{id}` consumer test (HTTP 409 duplicate title)
4. **E2E**
   - Should edit task through complete UI workflow
5. **Accessibility**
   - Should have proper labels and ARIA attributes for form fields
   - Should announce validation errors to screen readers
   - Should trap focus within modal
6. **UAT** - N/A

## Technical Notes
- Uses PrimeReact Dialog component (minimum width: 480px)
- Fetches task data on modal open via `getTask(taskId)`
- Calls `updateTask()` service function
- Sends PUT request to `/v1/tasks/{taskId}`
- Modal ID: `edit-task-modal`
- Button classes: `close-button`, `save-button`

