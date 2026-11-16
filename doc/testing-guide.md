# QA Testing Guide

A guide with actionable checklists to ensure complete test coverage following **risk-based test layering** ([ADR 007](adr/007-unit-vs-integration-test-boundary-checklist.md)): each scenario lives in exactly one layer, the public-boundary integration test by default, a unit test only for pure-logic permutations and failure injection. Use this when implementing new features or fixing bugs.

---

## 📋 General Principles

Before starting any testing work:
- [ ] Requirements are clear and acceptance criteria are defined
- [ ] Test data scenarios are identified (happy path, edge cases, error cases)
- [ ] Each scenario is assigned to exactly one layer by where its risk lives (see `.cursor/rules/common-testing.mdc` → "Choosing the Layer"); the resulting unit/integration/E2E ratio is an outcome, not a target
- [ ] User story **Test Plan** levels understood — see [ADR 003](adr/003-test-plan-integration-in-user-stories.md#automation-in-this-repo) (automation vs manual, CI scope)

---

## 🔗 Integration Testing (Primary Layer)

Every AC's happy path and every error path reachable through the public interface is proven here first. Before writing a unit test, name the integration test that would already catch the regression.

### Frontend Integration (Vitest)
- [ ] **Component Behaviour** - Conditional rendering, state changes, event handlers, prop/callback wiring — on the real component tree
- [ ] **Workflows** - Test complete user flows (create → list → edit → delete)
- [ ] **Form Submissions** - Test entire form lifecycle including API calls and validation feedback
- [ ] **Edge States** - Empty, loading, and error states as the user sees them
- [ ] **Error Handling** - Test API error responses and user feedback
- [ ] **Render Permutations** - Enum-driven labels/chips as `it.each` on the real component

**Standards:** Keep project dependencies real; mock only `fetch` and 3rd-party libs with browser-only behaviour  
**Detailed Rules:** `.cursor/rules/frontend-testing.mdc`

### Backend Integration (@SpringBootTest)
- [ ] **REST Endpoints** - Test all CRUD operations through HTTP
- [ ] **Database Integration** - Test with real MongoDB (TestContainers)
- [ ] **Request Validation** - Test `@Valid` with invalid payloads (400)
- [ ] **HTTP Status Codes** - Test 200, 201, 400, 404, 409 responses
- [ ] **Kafka Events** - Verify TaskEvent messages are produced correctly
- [ ] **Third-party Failures** - WireMock-stubbed validator errors → API error mapping

**Standards:** `@SpringBootTest` with full context; Testcontainers for infrastructure you own; WireMock for third parties  
**Detailed Rules:** `.cursor/rules/backend-testing.mdc`

---

## 🧩 Unit Testing (Pure Logic and Failure Injection)

A unit test exists only for what the integration layer cannot reach cheaply. Reuse from several callers is not a reason for one.

### Frontend (Vitest)
- [ ] **Services** - Request/response mapping and error translation with `fetch` mocked
- [ ] **Validation Rules** - Every branch of form validation and input constraints as pure functions
- [ ] **Hooks** - State logic via `renderHook`
- [ ] **Pure Utilities** - Data transformations, formatters
- [ ] **No component unit tests** - component behaviour belongs to the integration layer above

**Detailed Rules:** `.cursor/rules/frontend-testing.mdc`

### Backend (JUnit5)
- [ ] **Failure Injection** - Repository throws → no event published; external client errors → fallback/exception mapping
- [ ] **Logic Permutations** - Branches the HTTP suite cannot provoke cheaply (a handler branch such as the duplicate-key race). A branch the API already reaches (unchanged title vs. a title owned by another task) stays in the integration test
- [ ] **Adapters** - Outbound uri, content type, and JSON body are asserted in the API integration test (WireMock `equalToJson`). No Mockito test of the client fluent chain
- [ ] **Repository Queries** - Custom `@Query` / specifications only; standard CRUD is covered by ITs
- [ ] **No happy-path duplicates** - persist + publish, 404, 409 are proven by ITs; do not re-prove them with Mockito

**Standards:** Mock only I/O edges (repository, Kafka producer, external client); state-based assertions via the Test Context Pattern  
**Detailed Rules:** `.cursor/rules/backend-testing.mdc`

### Mutation testing as the safety net
- [ ] **Backend** - `mvn verify -Pmutation-tests` (CI gate; PIT runs unit and integration tests, Pact providers excluded); review `target/pit-reports/index.html`
- [ ] **Frontend** - `npm run test:stryker` (on-demand)
- [ ] **When moving or deleting tests** - run mutation testing with the integration suite included; a newly surviving mutant means the scenario was not covered at the other layer (see `common-testing.mdc` → "Moving or Deleting Tests")

---

## 🧱 Component Testing (Playwright CT)

### Playwright Component Tests
- [ ] **Complex UI Interactions** - Drag & drop, animations, transitions
- [ ] **Storybook Stories** - Test component variations and states
- [ ] **Visual Regression** - Snapshot tests for critical UI components
- [ ] **Hard-to-Test Scenarios** - Behaviors difficult to test at integration level

**When to use:** Only for complex UI that can't be tested in unit/integration

---

## 📝 Contract Testing (API Boundaries)

### Pact.io Consumer Tests (Frontend)
- [ ] **GET /v1/tasks** - Contract for fetching all tasks (200)
- [ ] **GET /v1/tasks/{id}** - Contract for fetching single task (200)
- [ ] **GET /v1/tasks/isValid/{id}** - Contract for external validation result (200)
- [ ] **POST /v1/tasks** - Contract for creating task (201)
- [ ] **PUT /v1/tasks/{id}** - Contract for updating task (200)
- [ ] **DELETE /v1/tasks/{id}** - Contract for deleting task (204)
- [ ] **Duplicate title (409)** - Contract for create/update conflict responses
- [ ] **Pact File Published** - Consumer pact uploaded to broker/repo

> **409** is the only error status in Pact — duplicate-title negative contracts on POST/PUT (the FE depends on the conflict response shape). **400** and **404** need no Pact tests; there are no negative contracts for them (covered in integration tests instead).

### Pact.io Provider Tests (Backend)
- [ ] **Provider Verification** - All consumer contracts verified
- [ ] **State Handlers** - Provider states implemented for test setup
- [ ] **Positive Cases** - Happy-path contracts pass
- [ ] **Negative Cases** - Duplicate-title (409) contracts pass

**Standards:** Validate API boundary between FE/BE; catch breaking changes early

---

## 🌐 E2E Testing (Critical Paths Only)

- [ ] **Critical Happy Path** - End-to-end user journey (create task → verify → edit → delete)
- [ ] **Authentication Flow** - Login/logout (if applicable)
- [ ] **Main Use Cases** - Top 3-5 most critical user scenarios
- [ ] **Cross-Browser** - Desktop Chromium and mobile WebKit

**Avoid:** Testing every permutation - this is what unit/integration tests are for

**Framework structure:** use domain-grouped `StepProvider` and `ValidationProvider` as the test-facing API (`steps.tasks`, `validate.task`, …). See [ADR 005](adr/005-domain-grouped-step-and-validation-providers-for-e2e.md).

---

## ♿ Accessibility Testing

- [ ] **Axe checks** - Automated axe-core scans on key UI states (task table, create/edit/info modals)

## 👁️ Manual UI Checks

- [ ] **Visual check** - Every frontend user story: layout, colours, icons, error/loading states (no screenshot or visual-regression automation in this repo)
- [ ] **Keyboard navigation** - Where listed in the story Manual section (tab order, modal focus)
- [ ] **Screen reader** - Where listed in the story Manual section (announcements, labels)


See [ADR 003](adr/003-test-plan-integration-in-user-stories.md) for the full test-plan level list.

---

## 🔧 External Service Testing

### WireMock (external validation)

Code-based stubs only — `POST /external/validate/task` → `true` / `false` (integration: `@EnableWireMock`; E2E: `WireMockClient` admin API). WireMock on port 8085 when using Docker.

### Kafka (`task-event` topic)

- [ ] **Producer** - `CREATED` / `UPDATED` / `DELETED` events after API calls (integration tests). Unit tests cover send failure, once, and "publish nothing" on the error paths the controller handles
- [ ] **Payload** - `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`
- [ ] **Contract** - Pact message contract with `notification-service` (consumer not tested against live Kafka)

---

## 📊 Test Coverage & Reporting

### Coverage Checks
- [ ] **Line coverage** - ≥90% measured across unit + integration together (JaCoCo / Istanbul / Kover)
- [ ] **Integration coverage** - Every AC happy path and error path reachable through the public interface
- [ ] **E2E coverage** - Critical paths only
- [ ] **Mutation testing** - PiTest ≥80% (BE, in CI); Stryker on-demand (FE, manual only)
- [ ] **Reports** - Review coverage and mutation reports when changing test suites

### Test Reporting
- [ ] **Allure Report** - Generate comprehensive test report
- [ ] **Playwright Report** - HTML report for E2E tests with screenshots
- [ ] **CI Pipeline** - Tests run and report in GitHub Actions

---

## 🚀 CI/CD Integration

### Pipeline Checks

All workflows trigger on **push or pull request to `master` only** — pushes to other branches do not run CI unless opened as a PR targeting `master`. Most workflows are also **path-filtered** (they skip when unrelated files change).

| Trigger | What runs |
|---|---|
| Push / PR to `master`, `demo-service/**` (or `docker/**`) | `demo-service` — unit, PiTest mutation, unit + integration with JaCoCo ≥90% (excl. Pact provider) |
| Push / PR to `master`, `demo-interface/**` | `demo-interface` — Vitest unit + integration |
| Push / PR to `master`, `demo-android/**` | `demo-android` — JVM unit + integration tests (Robolectric; pact excluded) |
| Push / PR to `master`, relevant consumer / provider paths | `pact-interface` — `demo-interface` consumer, task API provider verify, can-i-merge |
| Push / PR to `master`, relevant consumer / provider paths | `pact-notification` — `notification-service` consumer, events provider verify, can-i-merge |
| Push / PR to `master`, relevant consumer / provider paths | `pact-android` — `demo-android` consumer, task API provider verify, can-i-merge |
| Push / PR to `master`, `demo-interface/**` / `demo-service/**` / `e2e/**` | Playwright + Selenide — mocked BE, accessibility, UAT |
| Push / PR to `master` | `codeql` — SAST (see Security below) |

- [ ] **All required workflows green** before merging to `master`

### Security (GitHub-native)
- [ ] **SCA (Dependabot alerts)** — `dependabot-check` green (open moderate/high/critical alerts must be gone from this tree). GitHub's Dependabot UI may still list them until merge + rescan (platform setting; no `dependabot.yml` version-update PRs)
- [ ] **Secret scanning** — Push protection enabled on the repo; no leaked credentials in PRs (platform setting)
- [ ] **SAST (CodeQL)** — `.github/workflows/codeql.yml` green on PR; review SARIF artifacts if needed

---

## ✅ Definition of Done

A ticket is **DONE** when:
- [ ] Applicable checklist items above are implemented (unit → integration → contract/E2E as needed)
- [ ] All tests pass locally
- [ ] Line coverage ≥90% (unit + integration); backend PiTest ≥80% when Java logic changed
- [ ] CI green — required workflows on the PR to `master` (path-filtered where applicable)
- [ ] Code reviewed and approved
- [ ] README or this guide updated if test commands or CI changed

---

## 📖 Additional Resources

- **[Quick Checklist](check-list.md)** - Simple test-layer reminder for daily work
- **[ADR Index](adr/README.md)** - Architecture decisions (Vitest, test context pattern, integration strategy, and more)
- **[Private testing rules](../README.md#-documentation)** - Detailed FE/BE unit & integration rules (private submodule; available on request)
