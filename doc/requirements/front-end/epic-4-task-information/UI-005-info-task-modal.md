# UI-005: Task Detail View

**Points:** 3 · **Epic:** Task Information

As a user, I want a read-only detail view with full task details and validation status.

## Acceptance Criteria

1. Should open detail view titled with task name; spinner while loading
2. Should show read-only: description ("No description" if empty), status/priority tags (consistent with list), created/updated dates (locale-aware formatting), validation icon
3. Should fetch task (`GET /v1/tasks/{id}`) and validation (`GET /v1/tasks/isValid/{id}`) independently; green check / red X for valid/invalid
4. Should close detail view and return to list; remain usable if either fetch fails

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should open detail view and display task details for all-values and required-values datasets
   - Should show both validation outcomes (valid and not valid indicators)
   - Should close detail view on close action
   - Should keep detail flow available when task details request fails (HTTP 500 or network rejection)
   - Should keep detail flow available when validation request fails (HTTP 500 or network rejection)
   - Should have translations for detail view
3. **Pact**
   - Should have GET `/v1/tasks/{id}` consumer test (HTTP 200)
   - Should have GET `/v1/tasks/isValid/{id}` consumer test (HTTP 200 with boolean)
4. **E2E**
   - Should view task detail through UI
5. **Accessibility**
   - axe-core checks on detail view in E2E
6. **UAT** - N/A
7. **Manual**
   - Visual check
   - Focus management within detail view
