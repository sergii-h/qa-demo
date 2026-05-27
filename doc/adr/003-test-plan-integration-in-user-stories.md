# ADR 003: Test Plan Integration in User Stories

## Status
**Accepted** — February 17, 2026

## Context

User stories define acceptance criteria, but without an explicit test plan it is unclear which scenarios belong at which pyramid level. That leads to duplicated E2E coverage, gaps at lower levels, and test strategy drifting away from requirements.

Separate test documents also go stale quickly.

## Decision

**Add a Test Plan section to every user story** listing scenarios (or `N/A`) per test level.

**Standard levels:**

| # | Level | Backend | Frontend |
|---|-------|---------|----------|
| 1 | **UT** | JUnit + Mockito | Vitest |
| 2 | **IT** | RestAssured + `@SpringBootTest` (real MongoDB/Kafka) | Vitest + RTL — full component tree, mocked fetch; translation tests |
| 3 | **Pact** | Provider verification when FE consumes the API | Consumer contracts when calling BE APIs |
| 4 | **E2E** | Cross-reference only (see below) | Playwright / Selenide — critical UI workflows |
| 5 | **Accessibility** | N/A | axe-core in E2E suites (**automated**) |
| 6 | **UAT** | N/A | Full-stack smoke (`@uat`) |
| 7 | **Manual** | N/A | **Visual check** on every UI story; keyboard navigation and screen-reader checks **where listed** |

**Backend E2E (line 4) is not a separate test layer.** RestAssured IT tests already exercise the full HTTP stack. Use line 4 to document where UI coverage lives: `N/A`, *Covered by [workflow/story]*, or a pointer to the FE E2E that exercises this API.


**Every story lists all seven levels.** When a level does not apply, write **`N/A`**.

**When writing the plan:** UT covers ACs at the lowest feasible level; IT covers public-facing integration (see ADR 004); E2E only for critical paths; unused levels get explicit `N/A`.

### Automation in this repo

| Level | Automated? | Notes |
|-------|------------|--------|
| **UT**, **IT**, **Pact**, **E2E** | Yes (CI) | IT includes `*.translation.test.tsx` for localized FE components |
| **Accessibility** | Yes | axe-core only, via E2E suites |
| **UAT** | Yes | Full-stack smoke on `master` (`@uat`); most stories `N/A` |
| **Manual** | No | FE only — visual check; keyboard/screen-reader when listed |


## Rationale

- **Shift-left** — plan forces most coverage into UT/IT before E2E
- **Single source of truth** — test strategy lives with the story, not a separate doc
- **No duplication** — validation at UT; API contracts at Pact; full UI journeys at E2E once
- **Clear ownership** — developers and QA read the same checklist per story
- **Explicit manual scope** — visual and assistive-tech checks are named

**Not chosen:** ad-hoc testing; separate test specs; defaulting everything to E2E.

## Examples

**Backend — `TASK-001` (abbreviated):**

```markdown
## Test Plan
1. **UT** - All ACs covered at unit level (or IT where unit isolation is not feasible)
2. **IT** - POST `/v1/tasks` → 201; 409 duplicate title; 400 validation errors
   - **Note:** Kafka publishing tested in EVENT-001 IT
3. **Pact** - Provider: POST `/v1/tasks` → 201 and 409
4. **E2E** - Create task through complete UI workflow (FE story)
5. **Accessibility** - N/A
6. **UAT** - Covered by create-task UAT smoke (real POST against full stack)
7. **Manual** - N/A
```

**Frontend — `UI-003` (abbreviated):**

```markdown
## Test Plan
1. **UT** - All ACs covered at unit level
2. **IT** - Modal render/validation; create flow with mocked API; error/retry paths; translations
3. **Pact** - Consumer: POST `/v1/tasks` → 201 and 409
4. **E2E** - Complete create workflow (open modal → fill → save → verify table)
5. **Accessibility** - axe-core checks on create modal in E2E
6. **UAT**
   - Should create task via full-stack smoke (`@uat`)
7. **Manual**
   - Visual check
   - Focus trap within modal
   - Screen reader announcements for validation errors
```

Story template and full examples: `doc/requirements/`.

## Consequences

### Positive ✅
- Visible per-story test expectations for all stakeholders
- Pyramid-friendly distribution enforced at planning time
- Explicit `N/A` reduces forgotten levels
- Automated vs manual checks are unambiguous

### Negative ⚠️
- Test plans must be updated when strategy or ACs change
- Team must understand pyramid levels and when to stop at IT

## References

- **Requirements:** `doc/requirements/` (backend and frontend epics)
- [ADR 001](001-vitest-over-jest.md) — frontend UT/IT tooling
- [ADR 002](002-test-context-pattern-with-object-comparison.md) — backend UT/IT data pattern
- [ADR 004](004-api-level-integration-tests-as-sole-integration-layer.md) — what “IT” means for BE/FE
- [Testing guide](../testing-guide.md)
- [Private testing rules](../../README.md#-documentation) — `.cursor/rules` submodule
- [Test Pyramid (Martin Fowler)](https://martinfowler.com/articles/practical-test-pyramid.html)
