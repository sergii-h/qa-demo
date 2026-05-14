# UI-003: Create Task Modal

**Epic:** Task Creation  
**Priority:** High  
**Story Points:** 5

## Description
As a user, I want to create a new task through a modal dialog so that I can add tasks to my task list with all required information.

## Acceptance Criteria

1. Should open modal dialog with title "New task" when user clicks "Create task" button

2. Should display form with following fields:
   - Title (text input, required, with asterisk)
   - Description (textarea, optional, 5 rows)
   - Status (dropdown, required, default: TODO)
   - Priority (dropdown, required, default: MEDIUM)

3. Should provide status dropdown options:
   - To Do (TODO)
   - In Progress (IN_PROGRESS)
   - Done (DONE)

4. Should provide priority dropdown options:
   - Low (LOW)
   - Medium (MEDIUM)
   - High (HIGH)

5. Should close modal and refresh tasks table with new task after successful creation with valid data

6. Should validate title is not empty before allowing submission

7. Should disable Create button when title is empty

8. Should display error message "Title is required" when title is blank and user attempts to create

9. Should display error message "Title must not exceed 100 characters" when title exceeds limit

10. Should display error "Task with this title already exists" when backend returns duplicate title error

11. Should display error message "Failed to create task. Please try again." for general errors

12. Should clear title error when user starts typing after error

13. Should show loading spinner during task creation

14. Should have Close and Create buttons in footer

15. Should close modal when Close button clicked without saving

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
   - Should have proper labels and ARIA attributes for form fields
   - Should announce validation errors to screen readers
   - Should trap focus within modal
6. **UAT** - N/A

## Technical Notes
- Uses PrimeReact Dialog component (minimum width: 480px)
- Form fields: InputText, InputTextarea, Dropdown components
- Calls `createTask()` service function
- Sends POST request to `/v1/tasks`
- Error styling with `p-invalid` class for invalid fields
- Test IDs: `create-task-title-input`, `create-button`, `close-button`, `title-error`, `loading-spinner`

