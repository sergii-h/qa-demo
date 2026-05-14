# UI-001: Tasks Table Display

**Epic:** Task Display  
**Priority:** High  
**Story Points:** 3

## Description
As a user, I want to view all my tasks in a table with visual status and priority indicators so that I can quickly scan and manage my task list.

## Acceptance Criteria

1. Should display tasks table with all existing tasks fetched from backend on initial page render

2. Should display tasks in a data table with striped rows for better readability

3. Should show task title column (30% width)

4. Should display status column (15% width) with colored tags:
   - TODO → Blue/Info tag
   - IN_PROGRESS → Orange/Warning tag
   - DONE → Green/Success tag

5. Should display priority column (15% width) with colored tags:
   - LOW → Green/Success tag
   - MEDIUM → Orange/Warning tag
   - HIGH → Red/Danger tag

6. Should show actions column with three buttons for each task:
   - Info button (outlined, with info icon)
   - Edit button (outlined, with pencil icon)
   - Delete button (outlined danger, with trash icon)

7. Should display "Create task" button at the top with plus icon

8. Should fetch tasks automatically on component mount

9. Should be responsive and adapt to different screen sizes

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should render tasks table with fetched data on component mount
   - Should display status and priority tags for all task variants
   - Should render action buttons for each task row
   - Should render empty table state when tasks response is empty
   - Should open create, info, and edit modals from table actions
   - Should send delete request with selected task ID when delete is triggered
   - Should delete task and remove row from table after successful delete response
   - Should keep task row when delete fails (HTTP 500 or network error)
   - Should allow delete retry after failure and remove row when retry succeeds
   - Should keep create flow available when initial tasks request fails
3. **Pact**
   - Should have GET `/v1/tasks` consumer test
4. **E2E**
   - Should render tasks table with fetched data on component mount
5. **Accessibility**
   - Should have proper ARIA labels for table and buttons
   - Should be keyboard navigable
6. **UAT** - N/A

## Technical Notes
- Uses PrimeReact DataTable component
- React functional component with hooks (useState, useEffect)
- Status and priority displayed using PrimeReact Tag component
- Fetches data from `GET /v1/tasks` endpoint
- CSS classes: `tasks-table`, `task-info-button`, `edit-task-button`, `delete-task-button`, `add-task-button`

