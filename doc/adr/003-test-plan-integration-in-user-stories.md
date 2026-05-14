# ADR 003: Test Plan Integration in User Stories

## Status
**Accepted** - February 17, 2026

## Context

In our QA demo project, we follow the testing pyramid and shift-left principles to ensure comprehensive test coverage across all layers. However, we needed a systematic way to document which tests should be implemented for each user story and at which level of the pyramid.

### The Problem

Without explicit test planning in user stories:
- **Unclear test ownership**: Developers/QA don't know which tests to write
- **Test duplication**: Same scenario tested at multiple levels unnecessarily
- **Missing coverage**: Edge cases tested at wrong level (or not at all)
- **Inconsistent approach**: Different stories tested differently
- **Hard to track**: No visibility into what tests exist for each feature

Traditional approaches:
- Separate test documentation → Gets out of sync with requirements
- Ad-hoc testing → Inconsistent coverage
- All tests at E2E level → Slow, expensive, fragile

## Decision

**Integrate a Test Plan section directly into each user story** specifying which test levels apply and what should be tested at each level.

### Test Plan Structure

Each user story includes a Test Plan section with 5-6 levels (depending on BE vs FE):

```markdown
## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests
2. **IT** - [List of integration test scenarios]
3. **Pact** - [Contract tests or N/A]
4. **E2E** - [Critical user workflows or N/A]
5. **Accessibility** - [Frontend only: ARIA, keyboard nav, etc.]
6. **UAT** - [Staging env tests or N/A]
```

### Backend Test Levels (5 levels, 3 applicable)

| Level | Tool | Scope | When |
|-------|------|-------|------|
| **1. UT** | JUnit + Mockito | All ACs at unit level | Always |
| **2. IT** | RestAssured + @SpringBootTest | Full API with real DB/Kafka | Always |
| **3. Pact** | Pact Provider Tests | Verify FE consumer contracts | When FE consumes API |
| **4. E2E** | N/A | Covered by IT layer | Never (redundant) |
| **5. UAT** | N/A | Full stack staging tests | Application-level only |

**Key Decision: E2E = N/A for Backend**
- RestAssured IT tests with full Spring Boot app **ARE** the backend E2E tests
- No need for separate E2E layer - IT already tests complete request-to-response flows
- E2E in traditional sense means "through the UI" which doesn't apply to APIs

### Frontend Test Levels (6 levels, 5-6 applicable)

| Level | Tool | Scope | When |
|-------|------|-------|------|
| **1. UT** | Vitest | Component unit tests | Always |
| **2. IT** | Vitest | Component integration with mocked services | Always |
| **3. Pact** | Pact Consumer Tests | API contract expectations | When consuming BE APIs |
| **4. E2E** | Playwright/Cypress | Full app with real browser | Critical user workflows |
| **5. Accessibility** | axe-core / pa11y | ARIA, keyboard nav, screen readers | All UI components |
| **6. UAT** | Playwright against staging | Full stack deployed | When staging env available |

**Key Decisions:**
- **E2E BE flexibility**: Can be mocked or real depending on BE ownership and maintenance complexity
- **Accessibility**: Only applicable to frontend (user-facing components)
- **UAT**: Only when staging environment exists

## Rationale

### Why This Approach?

**1. Shift-Left Principle Applied**
```
Test Plan explicitly pushes testing to lowest possible level:
- UT covers most ACs (fast, isolated)
- IT covers integration scenarios
- E2E only for critical paths
```

**2. Clear Test Ownership**
```markdown
Developer reads story → Sees "IT: Should return 404 when task not found"
QA reads story → Sees "E2E: Should create task through UI"
Everyone knows exactly what to implement.
```

**3. No Test Duplication**
```
Title validation tested at UT level → Not repeated at IT/E2E
Complete workflow tested at E2E → Not repeated at IT
```

**4. Visible in Requirements**
```
Test strategy is part of the story, not separate documentation
Stays in sync with acceptance criteria
Visible to all stakeholders
```

**5. Supports Test Pyramid**
```
     E2E (few)      ← Only critical paths
    /         \
   IT (more)        ← Complete journeys
  /           \
UT (most)           ← All ACs covered
```

## Examples

### Backend Example: TASK-001 (Create Task)

```markdown
## Test Plan

1. **UT** - All ACs covered at unit level
2. **IT**
   - Should create task with valid data → HTTP 201
   - Should return HTTP 409 when duplicate title
   - Should return HTTP 400 for validation errors
   - Should publish Kafka event to task-event topic
3. **Pact** - N/A (FE consumer tests cover this)
4. **E2E** - Complete UI create workflow (tested at FE level)
5. **UAT** - N/A
```

**Why this works:**
- ✅ All validation logic tested at UT (fast)
- ✅ Full API flow tested at IT with RestAssured
- ✅ Kafka integration verified at IT
- ✅ No redundant E2E layer (IT covers it)
- ✅ Clear what to implement

### Frontend Example: UI-003 (Create Task Modal)

```markdown
## Test Plan

1. **UT** - All ACs covered at unit level
2. **IT**
   - Should render modal with empty form and defaults
   - Should validate title (required, max length)
   - Should disable Create button when title empty
   - Should call createTask service with form data
   - Should handle duplicate title error
3. **Pact**
   - POST /v1/tasks → 201 with task response
   - POST /v1/tasks → 400 validation errors
   - POST /v1/tasks → 409 duplicate title
4. **E2E** - Complete create workflow (open modal → fill form → save → verify table)
5. **Accessibility**
   - Form labels and ARIA attributes
   - Focus trap within modal
   - Screen reader announcements
6. **UAT** - N/A (or staging tests when available)
```

**Why this works:**
- ✅ Component logic tested at UT/IT (Vitest)
- ✅ API contract verified at Pact level
- ✅ User workflow tested at E2E (Playwright)
- ✅ Accessibility explicitly called out
- ✅ No duplication across levels

## Consequences

### Positive

✅ **Clear Expectations**: Everyone knows what tests to write
✅ **No Duplication**: Each scenario tested once at appropriate level
✅ **Shift-Left Success**: Most tests at lower levels (fast feedback)
✅ **Visible Strategy**: Test approach is part of requirements
✅ **Easy Tracking**: Can see test coverage per story at a glance
✅ **Prevents Gaps**: Explicit N/A prevents forgetting test levels
✅ **Team Alignment**: Developers, QA, and stakeholders see same plan

### Negative

⚠️ **Maintenance**: Test plan needs updating if testing strategy changes
⚠️ **Discipline Required**: Team must follow the plan consistently

### Neutral

→ **Test plans are part of requirements**: Can't skip test planning phase (this is shift-left by design)
→ **Must understand test pyramid**: Team needs training on appropriate test levels

## Implementation

### When Creating User Stories

1. Write acceptance criteria (AC)
2. Immediately add Test Plan section
3. For each test level, ask:
   - **UT**: Can this be unit tested? (usually yes for all ACs)
   - **IT**: What integration scenarios need testing?
   - **Pact**: Does this involve API calls? (FE consumer or BE provider)
   - **E2E**: Is this a critical user workflow? (minimal coverage)
   - **Accessibility**: Is this user-facing UI? (frontend only)
   - **UAT**: Do we have staging environment? (rarely applicable per story)

### Story Template

```markdown
# STORY-ID: Story Title

**Epic:** Epic Name
**Priority:** High/Medium/Low
**Story Points:** X

## Description
As a [role], I want [action] so that [benefit].

## Acceptance Criteria
[Given-When-Then or "Should..." statements]

## Test Plan

1. **UT** - Ticket's functional ACs are covered with unit tests (or integration tests, if not possible at unit level)
2. **IT**
   - [Specific integration test scenarios]
3. **Pact**
   - [Contract tests] or N/A
4. **E2E**
   - [Critical workflows] or N/A or "Covered by [other story]"
5. **Accessibility** (Frontend only)
   - [ARIA labels, keyboard nav, etc.] or N/A
6. **UAT** (Optional)
   - N/A or [Staging environment tests]

## Technical Notes
[Implementation details]
```

## Related Decisions

- **ADR-001**: Vitest over Jest (explains UT/IT tool choice for frontend)
- **ADR-002**: Test Context Pattern (explains how to write maintainable unit tests)
- **Testing Standards**: `doc/testing-standards.md` (comprehensive testing guidelines)

## References

- Martin Fowler's Test Pyramid: https://martinfowler.com/articles/practical-test-pyramid.html
- Shift-Left Testing: https://en.wikipedia.org/wiki/Shift-left_testing
- Contract Testing with Pact: https://docs.pact.io/

---

**Decision Made By**: QA Team & Development Team  
**Stakeholders**: Product Owners, Developers, QA Engineers  
**Review Date**: February 17, 2027 (annual review)

