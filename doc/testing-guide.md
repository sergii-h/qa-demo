# QA Testing Guide

A guide with actionable checklists to ensure complete test coverage following the **Testing Pyramid** approach. Use this when implementing new features or fixing bugs.

---

## 📋 General Principles

Before starting any testing work:
- [ ] Requirements are clear and acceptance criteria are defined
- [ ] Test data scenarios are identified (happy path, edge cases, error cases)
- [ ] Testing approach follows Testing Pyramid (90% unit → 7-8% integration → <2% E2E)
- [ ] User story **Test Plan** levels understood — see [ADR 003](adr/003-test-plan-integration-in-user-stories.md#automation-in-this-repo) (automation vs manual, CI scope)

---

## 🧩 Unit Testing (90%+ Coverage)

### Frontend (Vitest)
- [ ] **Component Logic** - Test all conditional rendering, state changes, event handlers
- [ ] **Business Logic** - Test pure functions, utilities, data transformations
- [ ] **Edge Cases** - Test empty states, loading states, error states
- [ ] **Validation Rules** - Test form validation, input constraints (mandatory fields, max length, unique constraints)
- [ ] **Props & Callbacks** - Use spies to verify prop passing and function calls
- [ ] **Coverage Check** - Run `npm test -- --coverage` and verify >90% coverage

**Detailed Rules:** [Private `.cursor/rules` submodule](../README.md#-documentation)

### Backend (JUnit5)
- [ ] **Service Logic** - Test all business logic methods in isolation
- [ ] **Validation** - Test `@Valid` annotations, constraints, custom validators
- [ ] **Repository Queries** - Test custom query methods (e.g., `findByTitle`)
- [ ] **Exception Handling** - Test custom exceptions (e.g., `TaskNotFoundException`, `DuplicateTitleException`)
- [ ] **Edge Cases** - Test null handling, empty collections, boundary values
- [ ] **Mock External Dependencies** - Mock repositories, external clients, Kafka producers
- [ ] **Mutation Testing** - Run `mvn verify -Pmutation-tests -DskipITs` and review surviving mutants in `target/pit-reports/index.html`

**Standards:** Mock external dependencies; test business logic in isolation  
**Detailed Rules:** [Private `.cursor/rules` submodule](../README.md#-documentation)

---

## 🔗 Integration Testing (7-8% Coverage)

### Frontend Integration (Vitest)
- [ ] **Component Interactions** - Test parent-child component communication
- [ ] **API Integration** - Test service layer calls with real/mocked fetch
- [ ] **Workflows** - Test complete user flows (create → list → edit → delete)
- [ ] **Form Submissions** - Test entire form lifecycle including API calls
- [ ] **Error Handling** - Test API error responses and user feedback

**Standards:** Keep project dependencies real; mock only 3rd party libs  
**Detailed Rules:** [Private `.cursor/rules` submodule](../README.md#-documentation)

### Backend Integration (@SpringBootTest)
- [ ] **REST Endpoints** - Test all CRUD operations end-to-end
- [ ] **Database Integration** - Test with real MongoDB (TestContainers or local)
- [ ] **Request Validation** - Test `@Valid` with invalid payloads
- [ ] **HTTP Status Codes** - Test 200, 201, 400, 404, 409 responses
- [ ] **Kafka Events** - Verify TaskEvent messages are produced correctly

**Standards:** Use `@SpringBootTest` for full application context; test containers for DB/Kafka  
**Detailed Rules:** [Private `.cursor/rules` submodule](../README.md#-documentation)

---

## 🧱 Component Testing (1-2% Coverage)

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

## 🌐 E2E Testing (<2% Coverage)

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

- [ ] **Producer** - `CREATED` / `UPDATED` / `DELETED` events after API calls (unit + integration tests)
- [ ] **Payload** - `taskId`, `title`, `status`, `priority`, `timestamp`, `eventType`
- [ ] **Contract** - Pact message contract with `notification-service` (consumer not tested against live Kafka)

---

## 📊 Test Coverage & Reporting

### Coverage Checks
- [ ] **Unit coverage** - ≥90% line coverage (JaCoCo / Istanbul)
- [ ] **Integration coverage** - Key workflows covered
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
| Push / PR to `master`, `demo-service/**` (or `docker/**`) | `demo-service` — unit, PiTest mutation, integration (excl. Pact provider) |
| Push / PR to `master`, `demo-interface/**` | `demo-interface` — Vitest unit + integration |
| Push / PR to `master`, `demo-android/**` | `demo-android` — JVM unit + integration tests (Robolectric; pact excluded) |
| Push / PR to `master`, relevant consumer / provider paths | `pact-interface` — `demo-interface` consumer, task API provider verify, can-i-merge |
| Push / PR to `master`, relevant consumer / provider paths | `pact-notification` — `notification-service` consumer, events provider verify, can-i-merge |
| Push / PR to `master`, relevant consumer / provider paths | `pact-android` — `demo-android` consumer, task API provider verify, can-i-merge |
| Push / PR to `master`, `demo-interface/**` / `demo-service/**` / `e2e/**` | Playwright + Selenide — mocked BE, accessibility, UAT |
| Push / PR to `master` | `codeql` — SAST (see Security below) |

- [ ] **All required workflows green** before merging to `master`

### Security (GitHub-native)
- [ ] **SCA (Dependabot alerts)** — No open high/critical dependency alerts under **Security → Dependabot** (platform setting; no `dependabot.yml` version-update PRs)
- [ ] **Secret scanning** — Push protection enabled on the repo; no leaked credentials in PRs (platform setting)
- [ ] **SAST (CodeQL)** — `.github/workflows/codeql.yml` green on PR; review SARIF artifacts if needed

---

## ✅ Definition of Done

A ticket is **DONE** when:
- [ ] Applicable checklist items above are implemented (unit → integration → contract/E2E as needed)
- [ ] All tests pass locally
- [ ] Unit coverage ≥90%; backend PiTest ≥80% when Java logic changed
- [ ] CI green — required workflows on the PR to `master` (path-filtered where applicable)
- [ ] Code reviewed and approved
- [ ] README or this guide updated if test commands or CI changed

---

## 📖 Additional Resources

- **[Quick Checklist](check-list.md)** - Simple testing pyramid reminder for daily work
- **[ADR Index](adr/README.md)** - Architecture decisions (Vitest, test context pattern, integration strategy, and more)
- **[Private testing rules](../README.md#-documentation)** - Detailed FE/BE unit & integration rules (private submodule; available on request)
