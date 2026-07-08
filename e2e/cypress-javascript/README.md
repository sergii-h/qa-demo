# Cypress + JavaScript E2E Tests

Mirrors the Playwright TypeScript layout: **pages** (locators) → **steps** (actions) → **validators** (assertions), wired via `providers/` and `fixtures/providers.js`. API stubs use `cy.intercept` in `support/mocks/ApiRouteMock.js`.

## Prerequisites & setup

- Node.js 22+ via [asdf](https://asdf-vm.com/) (`.tool-versions` pins 22.22.3)
- UAT only: full stack from repo root — `docker compose -f docker/docker-compose/run-application.yml up -d`

```bash
cd e2e/cypress-javascript
asdf plugin add nodejs && asdf install   # first time
npm install
```

Config: `.env.e2e` (override with git-ignored `.env.e2e.local`).

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_TEST_ENV_URL` | `http://localhost:5173` | Frontend |
| `E2E_API_URL` | `http://localhost:8080` | Backend |
| `E2E_WIREMOCK_URL` | `http://localhost:8085` | WireMock |

## Running tests

| Suite | Command | Real backend |
|-------|---------|--------------|
| Mocked BE | `npm run test:e2e` | No |
| Accessibility | `npm run test:accessibility` | No |
| UAT | `npm run test:uat` | Yes |
| All | `npm test` | UAT only |

```bash
npm run test:e2e                      # sequential (default)
npm run test:e2e:parallel             # 3 workers — optional
npm run test:accessibility:parallel   # 2 workers
npm run test:open                     # Cypress UI (all suites)
npx cypress run --spec tests/create-task/create-task.cy.js
```

**Parallel / sharding** — local default is sequential; opt in with `:parallel` (`scripts/run-cypress-parallel.sh` + [cypress-split](https://github.com/bahmutov/cypress-split)). CI uses matrix shards (`SPLIT` / `SPLIT_INDEX`). Workers: e2e 3 · accessibility 2 · uat 1. Override locally: `CYPRESS_SPLIT=4 npm run test:e2e:parallel`. Shard Allure results merge via local script or `merge-cypress-allure-shards` CI action.

**Spec discovery** — no files registered in `package.json`; `CYPRESS_SUITE` selects by pattern:

| Suite | Pattern | `CYPRESS_SUITE` |
|-------|---------|-----------------|
| Mocked BE | `*.cy.js` (excl. `*.uat` / `*.axe`) | `e2e` |
| UAT | `*.uat.cy.js` | `uat` |
| Accessibility | `*.axe.cy.js` | `accessibility` |

Tags `@uat` / `@accessibility` filter specs in interactive mode (`@cypress/grep`).

## Viewports

Like Playwright, each suite runs on **desktop and mobile** (Chrome with viewport + user-agent emulation — Cypress does not run WebKit on Linux CI):

| `CYPRESS_DEVICE` | Viewport | Matches |
|------------------|----------|---------|
| `desktop` (default) | 1280×720 | Playwright Desktop Chrome |
| `mobile` | 390×844 | Playwright iPhone 12 Pro |

```bash
npm run cy:run:desktop    # desktop only
npm run cy:run:mobile     # mobile only
npm run test:e2e          # both (default for all suite scripts via cy:run)
```

## Reports & artifacts

```bash
npm run allure:serve   # after a test run
```

Allure **Environment** includes OS, Node, `E2E_TEST_ENV_URL`, and browser type/version.

Screenshots on failure + Allure are the primary debug output. **Video is off** (`video: false`); folders, compression, and the `after:spec` cleanup hook remain for re-enabling later.

## Fluent chaining

Selenide-style flows — steps return `this` or the next step object:

```javascript
step.tasks.openCreateTaskForm().fillForm(context.createTaskData()).submitForm();

support.mock.api
  .getTasks([response]).createTask(response)
  .getTask(response.id, response).getIsValid(response.id, true);
```

**Cypress caveats:**
- `openEditTaskForm` / `openTaskInfoForm` / `deleteTask` resolve task id asynchronously — call on their own line before chaining form actions.
- Edit task: register `support.mock.api` **between** `fillForm` and `submitForm` — queued commands run as enqueued, so `fillForm().submitForm()` fires the PUT before the intercept exists.
