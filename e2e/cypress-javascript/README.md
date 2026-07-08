# Cypress + JavaScript E2E Tests

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

From `e2e/cypress-javascript`:

```bash
# install Node.js (first time only)
asdf plugin add nodejs
asdf install

# install dependencies
npm install
```

`.tool-versions` pins Node.js 22.22.3 for asdf. After `asdf install`, `node` and `npm` resolve via asdf shims in this directory.

## Test Suites

| Suite | Command | Requires real backend |
|-------|---------|----------------------|
| Mocked BE (user flows) | `npm run test:e2e` | No |
| Accessibility (axe-core) | `npm run test:accessibility` | No |
| UAT smoke | `npm run test:uat` | Yes |
| All suites | `npm test` | For UAT only |

```bash
cd e2e/cypress-javascript

npm run test:e2e            # mocked BE — 3 parallel Chrome workers (local)
npm run test:e2e:serial     # mocked BE — single worker (debugging)
npm run test:accessibility  # axe-core — 2 parallel workers
npm run test:accessibility:serial
npm run test:uat            # smoke test against the real running app (single worker)
npm test                    # all suites sequentially
```

Override local worker count: `CYPRESS_SPLIT=4 npm run test:e2e`

### Parallel execution

| Where | Mechanism | Workers |
|-------|-----------|---------|
| Local | `scripts/run-cypress-parallel.sh` via `[cypress-split](https://github.com/bahmutov/cypress-split)` | e2e: 3 · accessibility: 2 · uat: 1 |
| CI | GitHub Actions matrix + `cypress-split` (`SPLIT` / `SPLIT_INDEX`) | same as local |

Shard Allure results are merged automatically (local script or `merge-cypress-allure-shards` CI action) before publishing.

## Interactive mode

```bash
npm run test:open
```

## Single spec

```bash
npx cypress run --spec tests/create-task/create-task.cy.js
```

## Reports

```bash
npm run allure:serve   # Allure report (after tests)
```

### Videos

**Disabled for now** (`video: false`) — specs are too fast for useful MP4s; use **screenshots on failure** and **Allure** instead.

Video infrastructure is kept for when the suite grows: `videosFolder`, `videoCompression`, and the `after:spec` hook that deletes videos for passing specs (only failures would be retained once `video: true`).

## Run application

From repo root:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d
```

## Architecture

Mirrors the Playwright TypeScript layout:

- **Page objects** — element lookups only (`interactions/pages/`)
- **Steps** — workflow actions (`interactions/steps/`)
- **Validators** — assertions only (`interactions/validators/`)
- **Providers** — wire layers into tests (`providers/`, `fixtures/providers.js`)
- **Mocks** — `cy.intercept` stubs (`support/mocks/ApiRouteMock.js`); fluent chaining in `beforeEach` (mirrors Selenide `ApiRouteMockClient`)

**Fluent chaining** (Selenide-style) — form workflows return the next step object so tests read as a single flow:

```javascript
step.tasks
  .openCreateTaskForm()
  .fillForm(context.createTaskData())
  .submitForm();

support.mock.api
  .getTasks([response])
  .createTask(response)
  .getTask(response.id, response)
  .getIsValid(response.id, true);
```

Steps that resolve a task id asynchronously (`openEditTaskForm`, `openTaskInfoForm`) stay on a separate line before chaining form actions.

When a mock must be registered **after** filling a form but **before** submit (edit task), keep `fillForm` and `submitForm` on separate lines with `support.mock.api` between them — Cypress executes queued commands as they are enqueued, so chaining `fillForm().submitForm()` would fire the API call before the intercept is set up.

Suite file naming — specs are discovered automatically from `tests/**/`; no need to register files in `package.json`:

| Suite | File pattern | npm script sets |
|-------|--------------|-----------------|
| Mocked BE | `*.cy.js` (not `*.uat` / `*.axe`) | `CYPRESS_SUITE=e2e` |
| UAT | `*.uat.cy.js` | `CYPRESS_SUITE=uat` |
| Accessibility | `*.axe.cy.js` | `CYPRESS_SUITE=accessibility` |

Optional tags (`@uat`, `@accessibility`) remain on tests for filtering in interactive mode via `@cypress/grep`.
