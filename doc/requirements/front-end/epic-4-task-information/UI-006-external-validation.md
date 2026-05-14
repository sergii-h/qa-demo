# UI-006: External Validation Display

**Epic:** Task Information  
**Priority:** Low  
**Story Points:** 2

## Description
As a user, I want to see if my task passes external validation rules so that I know if my task data meets system requirements.

## Acceptance Criteria

1. Should display validation status with visual indicator after task and validation data are loaded in info modal

2. Should call external validation endpoint `/v1/tasks/isValid/{taskId}`

3. Should display green check icon with label "Validated:" when validation response is `true`

4. Should display red X icon with label "Validated:" when validation response is `false`

5. Should fetch validation status independently from task data

6. Should display validation indicator at the bottom of task info

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should display validation status when info modal is opened
   - Should display validated state when external validation returns true
   - Should display not-validated state when external validation returns false
   - Should keep info flow available when validation endpoint returns HTTP 500
   - Should keep info flow available when validation endpoint request is rejected (network failure)
3. **Pact**
   - Should have GET `/v1/tasks/isValid/{id}` consumer test (HTTP 200 with boolean)
4. **E2E**
   - Covered by UI-005 (info modal E2E test)
5. **Accessibility**
   - Should use ARIA labels for validation icons
6. **UAT** - N/A

## Technical Notes
- Calls `getIsValid(taskId)` service function
- Returns boolean value from backend
- Uses PrimeIcons: `pi-check` (green) and `pi-times` (red)
- Validation happens asynchronously with task data fetch
- Located in InfoTaskModal component

