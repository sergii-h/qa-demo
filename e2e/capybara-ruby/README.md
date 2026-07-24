# Capybara + RSpec E2E Tests

Mirrors the Playwright TypeScript and Cypress JavaScript layout: **pages** (locators) → **steps** (actions) → **validators** (assertions), wired via `providers/` and `fixtures/providers.rb`. API stubs use puffing-billy in `support/mocks/api_route_mock.rb`.

## Prerequisites & setup

- Ruby 3.3+ via [asdf](https://asdf-vm.com/) (`.tool-versions` pins 3.3.6)
- Chrome/Chromium for headless Selenium
- UAT only: full stack from repo root — `docker compose -f docker/docker-compose/run-application.yml up -d`

```bash
cd e2e/capybara-ruby
asdf plugin add ruby && asdf install   # first time
bundle install
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
| Mocked BE | `bundle exec rake test:e2e` | No |
| Accessibility | `bundle exec rake test:accessibility` | No |
| UAT | `bundle exec rake test:uat` | Yes |
| All | `bundle exec rake test` | UAT only |

Accessibility scans inject vendored `vendor/axe.min.js` (axe-core 4.10.2) into the page — no CDN fetch at runtime.

```bash
bundle exec rake test:e2e                      # sequential (default)
bundle exec rake test:e2e:parallel             # local workers — optional
bundle exec rake test:accessibility:parallel   # local workers
CAPYBARA_DEVICE=desktop bundle exec rspec spec/create_task/create_task_spec.rb
```

**Parallel / sharding** — local default is sequential (`test:e2e`). Opt in with `:parallel` tasks (`scripts/run_rspec_parallel.sh`). CI runs the parallel path on a single runner (e2e 3 workers · accessibility 2 · uat 1). With only a handful of specs, local serial can still be faster than parallel because each worker pays Chrome + Billy startup cost.

**Spec discovery** — `CAPYBARA_SUITE` selects by filename pattern:

| Suite | Pattern | `CAPYBARA_SUITE` |
|-------|---------|------------------|
| Mocked BE | `*_spec.rb` (excl. `*_uat` / `*_axe`) | `e2e` |
| UAT | `*_uat_spec.rb` | `uat` |
| Accessibility | `*_axe_spec.rb` | `accessibility` |

RSpec metadata `:uat` / `:accessibility` tags filter suites.

## Viewports

Each suite runs on **desktop and mobile** (Chrome with viewport + user-agent emulation):

| `CAPYBARA_DEVICE` | Viewport | Matches |
|-------------------|----------|---------|
| `desktop` (default) | 1280×720 | Playwright Desktop Chrome |
| `mobile` | 390×844 | Playwright iPhone 12 Pro |

## Reports & artifacts

```bash
bundle exec rake allure:serve   # after a test run
```

Allure **Environment** includes OS, Ruby version, `E2E_TEST_ENV_URL`, and browser type/version.

Screenshots on failure are attached to Allure results.

## Fluent chaining

Selenide-style flows — steps return `self` or the next step object:

```ruby
step.tasks.open_create_task_form.fill_form(context.create_task_data).submit_form

support.mock.api
      .get_tasks([response]).create_task(response)
      .get_task(response[:id], response).get_is_valid(response[:id], true)
```

**Capybara caveats:**
- `open_edit_task_form` / `open_task_info_form` / `delete_task` resolve task id synchronously — call on their own line before chaining form actions.
- Edit task: register `support.mock.api` **between** `fill_form` and `submit_form`.
