# UI-001: Task List Display

**Points:** 3 · **Epic:** Task Display

As a user, I want a task list with status/priority tags and row actions so I can scan and manage my list.

## Acceptance Criteria

1. Should load all tasks from `GET /v1/tasks` when the list is shown
2. Should show each task with title, status tags (TODO, IN_PROGRESS, DONE), priority tags (LOW, MEDIUM, HIGH), and actions
3. Should show Info, Edit, Delete, and a create entry point
4. Should render an empty list when the response is empty; remain usable when initial GET fails

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should render task list with fetched data when the list is first shown
   - Should display status and priority tags for all task variants
   - Should render action buttons for each task
   - Should render empty list state when tasks response is empty
   - Should open create, info, and edit flows from list actions
   - Should send delete request with selected task ID when delete is triggered
   - Should remove task from list after successful delete response
   - Should keep task in list when delete fails (HTTP 500 or network error)
   - Should allow delete retry after failure and remove task when retry succeeds
   - Should keep create flow available when initial tasks request fails
   - Should have translations for task list
3. **Pact**
   - Should have GET `/v1/tasks` consumer test
4. **E2E**
   - N/A (covered during task management)
5. **Accessibility**
   - axe-core checks when list is exercised in E2E suite
6. **UAT** - Covered by create-task UAT smoke (list load, task visible after create)
7. **Manual**
   - Visual check — status/priority tag colours, list layout, empty state
   - Keyboard navigation for list and row actions
