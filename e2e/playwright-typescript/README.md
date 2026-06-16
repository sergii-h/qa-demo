# Playwright + TypeScript E2E Tests

## Prerequisites

- Node 22 (≥22.12.0)
- Application stack running for UAT tests: `docker compose -f docker/docker-compose/run-application.yml up -d`

## Configuration

Test configuration is read from `.env.e2e`. Override values by creating a `.env.e2e.local` file (git-ignored).

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_TEST_ENV_URL` | `http://localhost:5173` | Frontend base URL |
| `E2E_API_URL` | `http://localhost:8080` | Backend API base URL |
| `E2E_WIREMOCK_URL` | `http://localhost:8085` | WireMock base URL |

## Test Suites

| Suite | Command | Requires real backend |
|-------|---------|----------------------|
| Mocked BE (user flows) | `npm run test:e2e` | No |
| Accessibility (axe-core) | `npm run test:accessibility` | No |
| UAT smoke | `npm run test:uat` | Yes |
| All suites | `npm test` | For UAT only |

```bash
cd e2e/playwright-typescript && npm install
npm run test:e2e           # browser-level user flows with mocked backend
npm run test:accessibility  # axe-core WCAG scans
npm run test:uat            # smoke test against the real running app
npm test                    # all suites
```

## Reports

```bash
npm run allure:serve   # Allure report (after tests)
```

## Run/Debug by viewport

```bash
npm run test:desktop   # 1920×1080
npm run test:mobile    # iPhone viewport
npm run test:mobile -- --grep @accessibility   # mobile + tag filter
```

## Debug a single test

```bash
npx playwright test tests/create-task.spec.ts --project="Desktop Chrome (chromium)" --ui
```

## Run/Debug with IDE

- Copy `.env.e2e` to `.env.e2e.local` and adjust URLs if needed
- Run or debug individual tests via the Playwright VS Code extension or with `npm test`
