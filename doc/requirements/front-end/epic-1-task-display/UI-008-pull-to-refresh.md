# UI-008: Pull-to-Refresh Task List

**Points:** 2 · **Epic:** Task Display · **Platform:** Native clients only (web task list does not implement pull-to-refresh)

As a user on a native client, I want to pull down on the task list to reload tasks so I can see updates without leaving the screen.

## Acceptance Criteria

1. Should call `GET /v1/tasks` when the user completes a pull-to-refresh gesture on the task list
2. Should show a refresh indicator while the reload is in progress
3. Should replace the displayed list with the new response when refresh succeeds (including when items were added or removed server-side)
4. Should keep the existing tasks visible when refresh fails (HTTP error or network failure)
5. Should show error feedback on refresh failure without clearing the list
6. Should support pull-to-refresh on an empty list (list remains usable after initial load)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should show new task when pull-to-refresh returns updated list
   - Should keep existing tasks when pull-to-refresh returns same list
   - Should keep existing tasks when pull-to-refresh fails with server error
3. **Pact**
   - N/A (GET `/v1/tasks` covered by [UI-001](UI-001-tasks-table.md))
4. **E2E**
   - N/A (pull-to-refresh is exercised as test infrastructure in native E2E suites; behaviour covered by IT)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check — refresh indicator during pull gesture
