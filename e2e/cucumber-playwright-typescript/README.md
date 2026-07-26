# Cucumber + Playwright + TypeScript E2E Tests

## Prerequisites

- Node.js 22+ (managed with [asdf](https://asdf-vm.com/))
- Application stack running for UAT tests: `docker compose -f docker/docker-compose/run-application.yml up -d` (from repo root)

## Configuration

Test configuration is read from `.env.e2e`. Override values by creating a `.env.e2e.local` file (git-ignored).

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_TEST_ENV_URL` | `http://localhost:5173` | Frontend base URL |
| `E2E_API_URL` | `http://localhost:8080` | Backend API base URL |
| `E2E_WIREMOCK_URL` | `http://localhost:8085` | WireMock base URL |

## Setup

From `e2e/cucumber-playwright-typescript`:

```bash
# install Node.js (first time only)
asdf plugin add nodejs
asdf install

# install dependencies and Playwright browsers
npm install
npx playwright install chromium webkit
```

`.tool-versions` pins Node.js 22.22.3 for asdf. After `asdf install`, `node` and `npm` resolve via asdf shims in this directory.

## Folder layout

| Path | Role |
|------|------|
| `bdd/features/` | Gherkin scenarios (`.feature`) |
| `bdd/step-definitions/` | Cucumber bindings (`Given` / `When` / `Then`) |
| `interactions/pages/` | Page objects — element lookups only |
| `interactions/steps/` | Workflow action classes (click, fill, submit) |
| `interactions/validators/` | Assertions only |
| `providers/` | Wires `step`, `validate`, `support` into fixtures |
| `fixtures/` | Playwright + playwright-bdd test instance |

`interactions/steps/` and `bdd/step-definitions/` both use the word "step", but they are different layers: step definitions translate Gherkin into calls like `step.tasks.createTask.submitForm()`; interaction steps implement those UI actions.

## Test Suites

Gherkin scenarios and Cucumber step definitions live under `bdd/` (`bdd/features/`, `bdd/step-definitions/`). Workflow actions, page objects, and validators stay under `interactions/` — the same page object → step → validator architecture as [Playwright TypeScript](../playwright-typescript/README.md).

Tests run in parallel by default (`fullyParallel: true` in `playwright.config.ts`).

| Suite | Command | Requires real backend |
|-------|---------|----------------------|
| Mocked BE (user flows) | `npm run test:e2e` | No |
| Accessibility (axe-core) | `npm run test:accessibility` | No |
| UAT smoke | `npm run test:uat` | Yes |
| All suites | `npm test` | For UAT only |

```bash
cd e2e/cucumber-playwright-typescript

npm run test:e2e            # browser-level user flows with mocked backend
npm run test:accessibility  # axe-core WCAG scans
npm run test:uat            # smoke test against the real running app
npm test                    # all suites
```

## Run/Debug by viewport

```bash
npm run test:desktop   # 1920×1080
npm run test:mobile    # iPhone viewport
npm run test:mobile -- --grep @accessibility   # mobile + tag filter
```

## Single scenario / debug

```bash
npx bddgen
npx playwright test bdd/features/create-task/create-task.feature --project="Desktop Chrome (chromium)"
npx playwright test --grep "Should create task" --project="Desktop Chrome (chromium)" --ui
```

## Reports

```bash
npm run allure:serve   # Allure report (after tests)
```

## Run application

From repo root:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d
```

## Run/Debug with IDE

- Copy `.env.e2e` to `.env.e2e.local` and adjust URLs if needed
- Run `npm run bddgen` after changing feature files or step definitions
- Run or debug scenarios via the Playwright VS Code extension or with `npm test`
