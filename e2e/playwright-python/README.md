# Playwright + Python E2E Tests

## Prerequisites

- Python 3.12+ (managed with [asdf](https://asdf-vm.com/))
- Application stack running for UAT tests: `docker compose -f docker/docker-compose/run-application.yml up -d` (from repo root)

## Configuration

Test configuration is read from `.env.e2e`. Override values by creating a `.env.e2e.local` file (git-ignored).

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_TEST_ENV_URL` | `http://localhost:5173` | Frontend base URL |
| `E2E_API_URL` | `http://localhost:8080` | Backend API base URL |
| `E2E_WIREMOCK_URL` | `http://localhost:8085` | WireMock base URL |

## Setup

From `e2e/playwright-python`:

```bash
# install Python (first time only)
asdf plugin add python
asdf install

# install uv (first time only)
curl -LsSf https://astral.sh/uv/install.sh | sh

# install dependencies from the lock file
uv sync --frozen
uv run playwright install chromium webkit
```

`.tool-versions` pins Python 3.12.8 for asdf. `uv.lock` pins all Python package versions for reproducible installs locally and in CI.

To refresh the lock file after changing `pyproject.toml`:

```bash
uv lock
```

## Test Suites

Tests run in parallel via `pytest-xdist` (`-n auto` in `pyproject.toml`). Use `-n 0` for sequential runs (easier debugging).

| Suite | Command | Requires real backend |
|-------|---------|----------------------|
| Mocked BE (user flows) | `pytest -m "not uat and not accessibility"` | No |
| Accessibility (axe-core) | `pytest -m accessibility` | No |
| UAT smoke | `pytest -m uat` | Yes |
| All suites | `pytest` | For UAT only |

```bash
cd e2e/playwright-python

uv run pytest -m "not uat and not accessibility"   # browser-level user flows with mocked backend
uv run pytest -m accessibility                     # axe-core WCAG scans
uv run pytest -m uat                               # smoke test against the real running app
uv run pytest                                      # all suites
uv run pytest -n 0                                 # sequential (debugging)
```

## Viewports

Like TypeScript’s Playwright **projects**, each test runs on desktop and mobile in a single pytest invocation:

| Param | Browser | Device | Matches TypeScript project |
|-------|---------|--------|---------------------------|
| `[chromium]` | chromium | Desktop Chrome | Desktop Chrome (chromium) |
| `[webkit]` | webkit | iPhone 12 Pro | Mobile Safari (webkit) |

Configured in `conftest.py` via `browser_context_args` (device per `browser_name`). Default `addopts` in `pyproject.toml` includes both `--browser chromium --browser webkit`.

```bash
# default: desktop + mobile (10 mocked user-flow tests = 5 × 2)
uv run pytest -m "not uat and not accessibility"

# desktop only
uv run pytest -m "not uat and not accessibility" --browser chromium

# mobile only
uv run pytest -m "not uat and not accessibility" --browser webkit

# override device for all browsers in the run
uv run pytest --browser chromium --browser webkit --device "iPhone 14 Pro Max"
```

`is_mobile` is `true` for `webkit` (or when `--device` is set explicitly).

## Single test / debug

```bash
uv run pytest tests/create_task/test_create_task.py -v
uv run pytest tests/create_task/test_create_task.py -v --headed
```

## Reports

### Allure

```bash
allure serve allure-results
```

### Playwright trace viewer

`pytest-playwright` does not generate the `playwright-report/` HTML report used by `@playwright/test`. On failure, traces and screenshots are saved under `test-results/` (tracing is enabled via `--tracing retain-on-failure` in `pyproject.toml`).

```bash
# open the trace for a failed test (interactive HTML viewer)
uv run playwright show-trace test-results/<test-name>/trace.zip
```

To record traces for every test (not only failures):

```bash
uv run pytest --tracing on
```

## Run application

From repo root:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d
```
