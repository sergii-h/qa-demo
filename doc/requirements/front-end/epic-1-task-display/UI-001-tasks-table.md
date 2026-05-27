# UI-001: Tasks Table Display

**Points:** 3 · **Epic:** Task Display

As a user, I want a task table with status/priority tags and row actions so I can scan and manage my list.

## Acceptance Criteria

1. Should load all tasks from `GET /v1/tasks` on mount (striped DataTable)
2. Should show columns: title, status tags (TODO/info, IN_PROGRESS/warning, DONE/success), priority tags (LOW/success, MEDIUM/warning, HIGH/danger), actions
3. Should show Info, Edit, Delete (outlined danger), and top "Create task" buttons
4. Should render empty table when response is empty; remain usable when initial GET fails

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
   - N/A (covered during task management)
5. **Accessibility**
   - axe-core checks when table is exercised in E2E suite
6. **UAT** - Covered by create-task UAT smoke (table load, task visible after create)
7. **Manual**
   - Visual check — status/priority tag colours, table layout, empty state
   - Keyboard navigation for table and row actions

## Notes

- Test IDs: `task-title-{id}`, `delete-button-{id}`; classes: `tasks-table`, `add-task-button`
