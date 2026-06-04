# UI-006: External Validation Display

**Points:** 2 · **Epic:** Task Information

As a user, I want a validation indicator in the detail view so I know if external rules pass.

## Acceptance Criteria

1. Should call `GET /v1/tasks/isValid/{taskId}` independently of task fetch
2. Should show green check / red X with label `"Validated:"` in the detail view for `true` / `false`
3. Should remain usable when validation endpoint fails (HTTP 500 / network)

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if it is not possible to cover on unit level)
2. **IT**
   - Should display validation status when detail view is opened
   - Should display validated state when external validation returns true
   - Should display not-validated state when external validation returns false
   - Should keep detail flow available when validation endpoint returns HTTP 500
   - Should keep detail flow available when validation endpoint request is rejected (network failure)
3. **Pact**
   - Should have GET `/v1/tasks/isValid/{id}` consumer test (HTTP 200 with boolean)
4. **E2E**
   - Covered by UI-005 (detail view E2E test)
5. **Accessibility** - N/A
6. **UAT** - N/A
7. **Manual**
   - Visual check
