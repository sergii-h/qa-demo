# QA Demo Project - Task Management Application

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.12-brightgreen?logo=spring)](https://spring.io/projects/spring-boot)
[![Node](https://img.shields.io/badge/Node-22-green?logo=node.js)](https://nodejs.org/)
[![React](https://img.shields.io/badge/React-18-blue?logo=react)](https://reactjs.org/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5-blue?logo=typescript)](https://www.typescriptlang.org/)
[![MongoDB](https://img.shields.io/badge/MongoDB-4.4-green?logo=mongodb)](https://www.mongodb.com/)
[![Kafka](https://img.shields.io/badge/Kafka-3.3.2-black?logo=apache-kafka)](https://kafka.apache.org/)

[![Vitest](https://img.shields.io/badge/Unit-Vitest-yellow?logo=vitest)](https://vitest.dev/)
[![JUnit5](https://img.shields.io/badge/Unit-JUnit5-green?logo=junit5)](https://junit.org/junit5/)
[![Selenide](https://img.shields.io/badge/E2E-Selenide-blue)](https://selenide.org/)
[![Playwright](https://img.shields.io/badge/E2E-Playwright-green?logo=playwright)](https://playwright.dev/)
[![Coverage](https://img.shields.io/badge/Coverage-90%25-brightgreen)](doc/testing-guide.md)
<!-- When adding a new E2E framework: add a badge here, a sub-bullet in Tech Stack > Testing Frameworks,
     a new #### section under Running Tests > E2E Tests, and replace the matching placeholder in Project Structure -->

[![Testing Pyramid](https://img.shields.io/badge/Strategy-Testing%20Pyramid-blue)](doc/testing-guide.md)
[![Shift-Left](https://img.shields.io/badge/Methodology-Shift--Left-blue)](doc/testing-guide.md)
[![Live Reports](https://img.shields.io/badge/Live%20Reports-GitHub%20Pages-blue?logo=github)](https://sergii-h.github.io/qa-demo/)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

A full-stack QA engineering demo project applying the **Testing Pyramid** and **shift-left** methodology.

<p align="center">
  <img src="doc/assets/qa-demo-app.gif" alt="QA Demo App" width="800"/>
</p>

---

## 📑 Table of Contents

- [Project Overview](#-project-overview)
- [Tech Stack](#-tech-stack)
- [Testing Strategy](#-testing-strategy)
- [CI/CD](#-cicd)
- [Getting Started](#-getting-started)
- [Running Tests](#-running-tests)
- [API Endpoints](#-api-endpoints)
- [Project Structure](#-project-structure)
- [Documentation](#-documentation)

---

## 📋 Project Overview

A full-stack Task Management app used as a testing pyramid demo. The domain is intentionally simple so all complexity lives in the test strategy.

**Task model:** `id` · `title` (unique) · `description` · `status` (TODO/IN_PROGRESS/DONE) · `priority` (LOW/MEDIUM/HIGH) · `createdDate` · `updatedDate`

**Requirements:** [Backend](doc/requirements/back-end/README.md) | [Frontend](doc/requirements/front-end/README.md)

---

## 🏗️ Tech Stack

| Layer | Stack |
|---|---|
| Backend | Java 21 · SpringBoot 3.5.12 · MongoDB 4.4 · Kafka 3.3.2 · WireMock 3.9.2 |
| Frontend | TypeScript 5 · React 18 · PrimeReact 10 · Vite 8 · Node 22 |
| Unit | JUnit5 + Mockito (BE) · Vitest 4 (FE) |
| Integration | JUnit5 + TestContainers (BE) · Vitest 4 (FE) |
| Contract | Pact — HTTP (FE↔BE) + Kafka message (notification-service↔BE) |
| E2E | Selenide + JUnit5 + Selenium Grid (Java) · Playwright + TypeScript |
| Mutation | PiTest (BE) · Stryker (FE, on-demand) |
| Performance | k6 |
| Security | CodeQL (SAST) · Dependabot alerts (SCA) · GitHub secret scanning & push protection (platform) |
| Coverage | JaCoCo ≥90% (BE) · Istanbul ≥90% (FE) |

---

## 🧪 Testing Strategy

```
           /\
          /  \    E2E — Full Stack
         /____\   Smoke tests on real env (P1 happy paths only)
        /      \
       /________\ E2E — FE + Mocked BE (Playwright)
      /          \ User flows — no real BE needed
     /____________\
    /              \ Integration + Contract (Pact)
   /________________\ API integration, consumer-driven contracts
  /                  \
 /____________________\
                        Unit (Vitest, JUnit5)
                        Business logic & component behavior
                        Target: >90% coverage
```

| Layer | Share | Notes |
|---|---|---|
| Unit | ~90% | Business logic, component behavior |
| Integration + Pact | ~7-8% | API integration; Pact decouples FE↔BE verification |
| E2E — Mocked BE | ~1-2% | Playwright `page.route()` — fast, no real BE required |
| E2E — Full Stack | <1% | Smoke only — confirms deployment wiring, not business logic |

### E2E Test Suites

Each E2E framework under `e2e/` follows the same three-suite split regardless of language or tool:

| Suite | Tag | Purpose | When to run |
|---|---|---|---|
| Mocked BE | *(no tag)* | Browser-level user flows with mocked backend | Every CI run |
| Accessibility | `@accessibility` | axe-core scans for WCAG violations on key UI states | Every CI run |
| UAT | `@uat` | Single smoke test against the real running app | Staging / post-deploy only |

The UAT suite is intentionally one test (the most critical happy path). Business logic is already covered by the layers below; UAT exists only to confirm all services are wired together correctly in a real environment.

📚 **Detailed Testing Standards:** See [doc/testing-guide.md](doc/testing-guide.md)

---

## 🔄 CI/CD

GitHub Actions validates every change — see [Actions](https://github.com/sergii-h/qa-demo/actions).

| Workflow | Scope |
|---|---|
| `demo-service` | Backend unit, PiTest mutation, integration — push / PR to `master`, path-filtered |
| `demo-interface` | Frontend unit + integration — push / PR to `master`, path-filtered |
| `demo-android` | Android unit + integration tests — push / PR to `master`, path-filtered |
| `pact-interface` | `demo-interface` consumer contracts, task API provider verify, can-i-merge — push / PR to `master` |
| `pact-notification` | `notification-service` consumer contracts, events provider verify, can-i-merge — push / PR to `master` |
| `pact-android` | `demo-android` consumer contracts, task API provider verify, can-i-merge — push / PR to `master` |
| `e2e-reports` | Publish Allure and Playwright reports to GitHub Pages after all web and Android E2E workflows finish |
| `allure-pages` | Allure reports landing page (`master`) |
| `allure-pages-cleanup` | Remove PR report folder (Allure + Playwright HTML) from GitHub Pages when a PR closes |
| `codeql` | SAST — CodeQL analysis for Java and TypeScript (`master`); SARIF artifacts in workflow runs |

**GitHub platform security** (no repo config): Dependabot alerts (SCA), secret scanning, push protection.

Allure and Playwright HTML reports from E2E runs are published to [GitHub Pages](https://sergii-h.github.io/qa-demo/):

| Trigger | Location | How to find |
|---|---|---|
| Push to `master` | `https://sergii-h.github.io/qa-demo/` | Landing page links to Allure (`{suite}/`) and Playwright HTML (`playwright-html-{suite}/`) |
| Pull request to `master` | `https://sergii-h.github.io/qa-demo/pr/{number}/` | Single PR comment with link after all E2E workflows finish; removed when the PR closes |

Raw Allure results and Playwright HTML reports are also kept as workflow artifacts for 7 days.

---

## 🚀 Getting Started

**Prerequisites:** JDK 21 · Maven 3.9+ · Node 22 (≥22.12.0) · Docker · k6 (performance tests only)

One-time setup:

```bash
docker network create qa-demo-e2e
```

### Run the full application in Docker

Starts backend, frontend, MongoDB, Kafka, and WireMock — open http://localhost:5173 (API at http://localhost:8080/v1/tasks).

```bash
docker compose -f docker/docker-compose/run-application.yml up -d

# Optional: Kafka consumer (processes task events from demo-service)
cd notification-service && mvn spring-boot:run
```

### Run locally for development

Start dependencies in Docker, then run backend and frontend on the host:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-mongo qa-demo-kafka qa-demo-wiremock

cd demo-service && mvn spring-boot:run          # http://localhost:8080/v1/tasks
cd demo-interface && npm install && npm start   # http://localhost:5173

# Optional: custom backend URL for the frontend
VITE_BE_API=http://localhost:8080/v1 npm start
```

---

## 🧪 Running Tests

### Unit & Integration Tests

**Backend:**
```bash
cd demo-service
mvn verify -DskipITs                                                                      # unit + coverage (JaCoCo)
mvn verify -Pintegration-tests -Dfailsafe.excludes='**/*PactProviderTest.java' -Djacoco.skip=true  # integration
mvn verify -Pmutation-tests -DskipITs                                                     # mutation (PiTest ≥80%)
open target/site/jacoco/index.html   # coverage report
open target/pit-reports/index.html  # mutation report
```

**Frontend:**
```bash
cd demo-interface
npm test              # unit + coverage (Istanbul)
npm run test:ui       # interactive Vitest UI
```

> Stryker mutation testing is configured (`stryker.config.json`) but excluded from the regular test run — it is too slow and resource-intensive to run on every CI build. Run manually with `npm run test:stryker` when validating assertion quality.


### Pact (Consumer-Driven Contract Tests)

This demo uses an **ephemeral Pact Broker** started fresh in Docker on each CI run. CI bootstraps `master` contracts first so `can-i-merge` has a baseline to compare against.

Each consumer runs in its own GitHub Actions workflow when its app (or `demo-service`) changes:

| Workflow | Consumer | Provider surface |
|----------|----------|------------------|
| `pact-interface.yml` | `demo-interface` | Task HTTP API |
| `pact-notification.yml` | `notification-service` | Task events (async) |
| `pact-android.yml` | `demo-android` | Task HTTP API |

```bash
# Run the full Pact pipeline locally (all consumers → publish → provider verify → can-i-merge)
bash .github/scripts/pact-run-local.sh

# Android-only pipeline
bash .github/scripts/pact-run-local-android.sh
```

Or run each phase manually:

```bash
# 1) Start broker
cd demo-interface && npm run pact:broker:up

# 2) Frontend consumer contracts
npm run test:pact
PACT_CONSUMER_VERSION=$(git rev-parse --short HEAD) npm run pact:publish

# 3) Notification service consumer contract
cd ../notification-service && mvn test
# then publish via pact-cli docker image (see pact-run-local.sh for the full command)

# 4) Provider verification
cd ../demo-service
PACT_BROKER_BASE_URL=http://localhost:9292 mvn verify -Pintegration-tests -Dit.test="*PactProviderTest" -Djacoco.skip=true

# 5) Can-i-merge gate
cd ../demo-interface
PACT_CONSUMER_VERSION=$(git rev-parse --short HEAD) npm run pact:can-i-deploy
```

> In production, a persistent broker (PactFlow or self-hosted) replaces the ephemeral approach and removes the bootstrap step.

### E2E Tests

#### Playwright + TypeScript

```bash
cd e2e/playwright-typescript && npm install

npm run test:e2e           # with mocked BE
npm run test:accessibility  # axe-core scans (no real backend needed)
npm run test:uat    # UAT smoke test (requires full app running)
npm test            # all suites
```

Copy `.env.e2e` to `.env.e2e.local` and set `E2E_TEST_ENV_URL` before running UAT tests.

```bash
npm run allure:serve  # Allure report (after tests)
```

See [Playwright E2E README](e2e/playwright-typescript/README.md) for full options.

#### Selenide + JUnit5 + Selenium Grid (Java)

```bash
# Local browser
cd e2e/selenide-junit5-selenium-grid
mvn -Dproperties.file.name=test.local.properties clean test
mvn -Pe2e clean test       # with mocked BE
mvn -Paccessibility clean test  # axe-core scans (no real backend needed)
mvn -Puat clean test  # UAT smoke test (requires full app running)

# Docker + Selenium Grid
docker network create qa-demo-e2e
docker compose -f docker/docker-compose/run-application.yml up -d
docker compose -f docker/docker-compose/run-selenium-grid.yml up -d
docker compose -f docker/docker-compose/run-tests-selenide-junit5-selenium-grid.yml up
```

See [Selenide E2E README](e2e/selenide-junit5-selenium-grid/README.md) for full options.

### Performance Tests (k6)

```bash
# Requires full app running
k6 run performance/baseline-load.js
k6 run performance/create-under-load.js
k6 run performance/concurrent-uniqueness.js
k6 run performance/spike-test.js
```

See [Performance README](performance/README.md) for thresholds and scenario details.

---

## 🎯 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/v1/tasks` | Get all tasks |
| GET | `/v1/tasks/{id}` | Get task by ID |
| POST | `/v1/tasks` | Create new task |
| PUT | `/v1/tasks/{id}` | Update task |
| DELETE | `/v1/tasks/{id}` | Delete task |
| GET | `/v1/tasks/isValid/{id}` | Validate task (external service) |

---

## 📁 Project Structure

```
qa-demo/
├── demo-service/              # SpringBoot Backend (Java 21)
│   ├── src/main/java/com/example/demo/
│   │   ├── data/              # Domain models (Task, TaskRequest, TaskEvent)
│   │   ├── TaskController.java
│   │   ├── TaskRepository.java
│   │   ├── TaskEventProducer.java
│   │   └── TaskExternalClient.java
│   └── src/test/java/         # Unit, integration & Pact provider tests
│
├── notification-service/      # Kafka Consumer (Java 21) — Pact message consumer
│
├── demo-interface/            # React Frontend (Vite 8 + Node 22)
│   └── src/
│       ├── components/        # tasksTable · createTaskModal · editTaskModal · infoTaskModal · languageSwitcher
│       ├── locales/           # i18n translations (en · es)
│       ├── interfaces/
│       └── services/          # API service layer
│
├── e2e/
│   ├── playwright-typescript/ # Playwright + TypeScript
│   │   ├── tests/             # Per-feature: *.spec.ts · *.axe.spec.ts · *.uat.spec.ts
│   │   ├── interactions/      # Page objects, step orchestrators, validators
│   │   ├── fixtures/          # Playwright fixtures
│   │   └── support/           # API client & mock helpers
│   └── selenide-junit5-selenium-grid/  # Selenide + JUnit5 (Java)
│       └── src/test/java/test/
│           ├── desktop/       # 1920×1080
│           └── mobile/        # iPhone viewport
│   # appium-*/                # (planned) Appium mobile testing
│
├── performance/               # k6 load & spike scripts
│
├── .github/                   # GitHub Actions workflows · reusable CI actions · Pact scripts
│
├── docker/                    # Docker Compose + Dockerfiles
│
└── doc/                       # Testing guide · ADRs · Requirements
```

---

## 📚 Documentation

| Doc | Contents |
|---|---|
| [Testing Guide](doc/testing-guide.md) | Public checklists, pyramid workflow, definition of done |
| Private testing rules | Detailed FE/BE unit & integration rules — private [`.cursor/rules`](.cursor/rules) submodule; available on request |
| [Performance README](performance/README.md) | k6 scenarios and thresholds |
| [ADR Index](doc/adr/README.md) | Architectural decisions with context and rationale |
| [Backend Requirements](doc/requirements/back-end/README.md) | Epics and user stories |
| [Frontend Requirements](doc/requirements/front-end/README.md) | Epics and user stories (web and mobile) |

---

## 📧 Contact

**[Sergii Holdys](https://github.com/sergii-h)** — QA Engineer  
**Location:** Malaga, Spain  
**LinkedIn:** [sergii-holdys](https://www.linkedin.com/in/sergii-holdys-501798158)  
**Available for:** Remote • Hybrid • On-site  
**Demo Project:** [View on GitHub](https://github.com/sergii-h/qa-demo)

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
