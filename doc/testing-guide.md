# QA Testing Guide

A comprehensive guide with actionable checklists to ensure complete test coverage following the **Testing Pyramid** approach. Use this when implementing new features or fixing bugs.

---

## 📋 General Principles

Before starting any testing work:
- [ ] Requirements are clear and acceptance criteria are defined
- [ ] Test data scenarios are identified (happy path, edge cases, error cases)
- [ ] Testing approach follows Testing Pyramid (90% unit → 7-8% integration → <2% E2E)

---

## 🧩 Unit Testing (90%+ Coverage)

### Frontend (Vitest)
- [ ] **Component Logic** - Test all conditional rendering, state changes, event handlers
- [ ] **Business Logic** - Test pure functions, utilities, data transformations
- [ ] **Edge Cases** - Test empty states, loading states, error states
- [ ] **Validation Rules** - Test form validation, input constraints (mandatory fields, max length, unique constraints)
- [ ] **Props & Callbacks** - Use spies to verify prop passing and function calls
- [ ] **Coverage Check** - Run `npm test -- --coverage` and verify >90% coverage

**Standards:** Follow [testing-standards.md](testing-standards.md) unit testing rules  
**Detailed Rules:** Available in the private rules repository (see project README)

### Backend (JUnit5)
- [ ] **Service Logic** - Test all business logic methods in isolation
- [ ] **Validation** - Test `@Valid` annotations, constraints, custom validators
- [ ] **Repository Queries** - Test custom query methods (e.g., `findByTitle`)
- [ ] **Exception Handling** - Test custom exceptions (e.g., `TaskNotFoundException`, `DuplicateTitleException`)
- [ ] **Edge Cases** - Test null handling, empty collections, boundary values
- [ ] **Mock External Dependencies** - Mock repositories, external clients, Kafka producers
- [ ] **Mutation Testing** - Run `mvn verify -Pmutation-tests -DskipITs` and review surviving mutants in `target/pit-reports/index.html`

**Standards:** Mock external dependencies; test business logic in isolation  
**Detailed Rules:** Available in the private rules repository (see project README)

---

## 🔗 Integration Testing (7-8% Coverage)

### Frontend Integration (Vitest)
- [ ] **Component Interactions** - Test parent-child component communication
- [ ] **API Integration** - Test service layer calls with real/mocked fetch
- [ ] **Workflows** - Test complete user flows (create → list → edit → delete)
- [ ] **Form Submissions** - Test entire form lifecycle including API calls
- [ ] **Error Handling** - Test API error responses and user feedback

**Standards:** Keep project dependencies real; mock only 3rd party libs  
**Detailed Rules:** Available in the private rules repository (see project README)

### Backend Integration (@SpringBootTest)
- [ ] **REST Endpoints** - Test all CRUD operations end-to-end
- [ ] **Database Integration** - Test with real MongoDB (TestContainers or local)
- [ ] **Request Validation** - Test `@Valid` with invalid payloads
- [ ] **HTTP Status Codes** - Test 200, 201, 400, 404, 409 responses
- [ ] **Kafka Events** - Verify TaskEvent messages are produced correctly

**Standards:** Use `@SpringBootTest` for full application context; test containers for DB/Kafka  
**Detailed Rules:** Available in the private rules repository (see project README)

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
- [ ] **GET /v1/tasks** - Contract for fetching all tasks
- [ ] **GET /v1/tasks/{id}** - Contract for fetching single task
- [ ] **POST /v1/tasks** - Contract for creating task
- [ ] **PUT /v1/tasks/{id}** - Contract for updating task
- [ ] **DELETE /v1/tasks/{id}** - Contract for deleting task
- [ ] **Error Responses** - Contracts for 404, 400, 409 errors
- [ ] **Pact File Published** - Consumer pact uploaded to broker/repo

### Pact.io Provider Tests (Backend)
- [ ] **Provider Verification** - All consumer contracts verified
- [ ] **State Handlers** - Provider states implemented for test setup
- [ ] **Positive Cases** - All happy path contracts pass
- [ ] **Negative Cases** - Error contracts (missing fields, duplicates) pass

**Standards:** Validate API boundary between FE/BE; catch breaking changes early

---

## 🌐 E2E Testing (<2% Coverage)

### Playwright E2E Tests
- [ ] **Critical Happy Path** - End-to-end user journey (create task → verify → edit → delete)
- [ ] **Authentication Flow** - Login/logout (if applicable)
- [ ] **Main Use Cases** - Top 3-5 most critical user scenarios
- [ ] **Cross-Browser** - Test on Chromium, Firefox, WebKit (CI only)

**Avoid:** Testing every permutation - this is what unit/integration tests are for

---

## ♿ Accessibility Testing

### Playwright Accessibility
- [ ] **Axe Checks** - Run accessibility scanner on key pages
- [ ] **Keyboard Navigation** - Tab through forms, modals, tables
- [ ] **Screen Reader** - Test aria-labels, semantic HTML
- [ ] **Color Contrast** - Verify WCAG AA compliance

**Tools:** Playwright Accessibility API, axe-core

---

## 🔧 External Service Testing

### WireMock Stubs
- [ ] **Valid Responses** - Stub successful external API responses
- [ ] **Error Responses** - Stub 404, 500, timeout scenarios
- [ ] **Mapping Files** - Update `docker/mappings/*.json` for new endpoints
- [ ] **Service Running** - Verify WireMock container is up on port 8085

### Kafka Event Testing
- [ ] **Event Production** - Verify TaskEvent published to `task-event` topic
- [ ] **Event Content** - Verify event contains correct data (taskId, title, status, priority, timestamp)
- [ ] **Event Types** - Test CREATED, UPDATED, DELETED events

---

## 📊 Test Coverage & Reporting

### Coverage Checks
- [ ] **Unit Coverage** - ≥90% line coverage for business logic
- [ ] **Integration Coverage** - Key workflows covered
- [ ] **E2E Coverage** - Critical paths only
- [ ] **Coverage Report** - Generate and review `npm test -- --coverage` or `mvn verify`

### Test Reporting
- [ ] **Allure Report** - Generate comprehensive test report
- [ ] **Playwright Report** - HTML report for E2E tests with screenshots
- [ ] **CI Pipeline** - Tests run and report in GitLab/GitHub Actions

---

## 🚀 CI/CD Integration

### Pipeline Checks
- [ ] **Unit Tests** - Run on every commit
- [ ] **Integration Tests** - Run on PR/MR
- [ ] **Contract Tests** - Verify on PR/MR
- [ ] **E2E Tests** - Run on staging/pre-production
- [ ] **Build Success** - All tests pass before merge

---

## 📚 Documentation

### Test Documentation
- [ ] **Test Cases** - Clear test descriptions and naming
- [ ] **README** - Update if new test commands added
- [ ] **Testing Standards** - Follow [testing-standards.md](testing-standards.md)
- [ ] **Pact Contracts** - Upload to contract repository/broker

---

## ✅ Definition of Done

A ticket is **DONE** when:
- [ ] All applicable tests from this checklist are implemented
- [ ] All tests pass locally
- [ ] Code coverage meets targets (>90% unit)
- [ ] Tests pass in CI pipeline
- [ ] Code reviewed and approved
- [ ] Documentation updated

---

---

## 📖 Additional Resources

- **[Testing Standards](testing-standards.md)** - Detailed testing rules and principles
- **[Quick Checklist](check-list.md)** - Simple testing pyramid reminder for daily work
- **AI Testing Rules** - 🤖 Comprehensive AI-ready testing rules (FE + BE) — available in the private rules repository (see project README)
- **[ADR-001: Vitest over Jest](adr/001-vitest-over-jest.md)** - Why we chose Vitest for unit/integration testing

