# playwright + typescript example tests

###### Tests
##### Configuration
`.env.e2e`

Test configuration from `.env.e2e` can be overridden by creating a `.env.e2e.local` file (git-ignored) with the values you want to change.

Available environment variables:
- `E2E_TEST_ENV_URL` - frontend base URL (default: `http://localhost:5173`)
- `E2E_API_URL` - backend API base URL (default: `http://localhost:8080`)
- `E2E_WIREMOCK_URL` - WireMock base URL (default: `http://localhost:8085`)

##### Run with npm
- all tests: `npm test`
- desktop only: `npm run test:desktop`
- mobile only: `npm run test:mobile`
- mobile only with tag: `npm run test:mobile -- --grep @accessibility`
- file only with debug `npx playwright test tests/create-task.spec.ts --project="Desktop Chrome (chromium)" --ui`
- open Allure report: `npm run allure:serve`

##### Run/Debug with IDE
- start application in background: `docker compose -f docker/docker-compose/run-application.yml up -d`
- copy `.env.e2e` to `.env.e2e.local` and adjust URLs if needed
- run or debug individual tests via the Playwright VS Code extension or with `npm test`
