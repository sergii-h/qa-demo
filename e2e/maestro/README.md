# Maestro React Native E2E Tests

Black-box E2E tests for `demo-react-native` using [Maestro](https://maestro.mobile.dev/) on Android and iOS. Mirrors the page object → step → validator architecture used by Playwright and Android Compose E2E suites.

## Prerequisites

- Node.js 22+ ([asdf](https://asdf-vm.com/) — see `.tool-versions`)
- [Maestro CLI](https://maestro.mobile.dev/docs/getting-started/installing-maestro)
- **Android:** SDK (`platform-tools`, emulator or device). Auto-detects `$HOME/Library/Android/sdk` (macOS) or `$HOME/Android/Sdk` (Linux), or `ANDROID_HOME` / `ANDROID_SDK_ROOT`.
- **iOS (macOS only):** Xcode + iOS Simulator
- Application stack for UAT tests (see below)

## Configuration

Test configuration is read from `.env.e2e`. Override values in `.env.e2e.local` (git-ignored).

| Variable | Default | Description |
|----------|---------|-------------|
| `E2E_WIREMOCK_URL` | `http://localhost:8085` | WireMock admin/API URL on the host |
| `E2E_API_URL` | `http://localhost:8080/v1` | Real backend API (UAT) |
| `MAESTRO_APP_ID` | `com.example.demo` | App id / bundle identifier |
| `MAESTRO_DEVICE` | _(unset)_ | Optional simulator/device UDID for Maestro and install |
| `MAESTRO_IOS_SIMULATOR` | _(unset)_ | Optional simulator name when none is booted (e.g. `iPhone 17`) |

Build the app with the API URL that matches your suite and platform:

| Suite | Android emulator | iOS Simulator |
|-------|------------------|---------------|
| Mocked BE / Accessibility | `http://10.0.2.2:8085/v1/` | `http://localhost:8085/v1/` |
| UAT | `http://10.0.2.2:8080/v1/` | `http://localhost:8080/v1/` |

The build scripts produce a **standalone release build** with an embedded JS bundle (no Metro). That is a different runtime from **`npx expo start`**, which loads JavaScript from Metro (Expo Go or dev client). Always validate E2E against the Maestro-built app, not Expo Go.

### Android

From `e2e/maestro` (start the emulator first):

```bash
npm run build:android:wiremock   # or build:android:uat
npm run install:android
```

The scripts resolve paths from their own location — they work from any directory, but running via npm keeps everything alongside `npm run test:e2e`.

Each build runs `expo prebuild`, verifies cleartext HTTP in the Android manifest, and auto-detects CPU architecture from the connected emulator (`arm64-v8a` / `x86_64`).

### iOS (macOS)

From `e2e/maestro` (a simulator is booted automatically if none is running):

```bash
npm run build:ios:wiremock   # or build:ios:uat
npm run install:ios
```

CocoaPods runs via `npx pod-install` (Expo). React Native may still print a deprecation notice about raw `pod install` — that is informational and safe to ignore.

Optional: pin a simulator by name in `.env.e2e.local`:

```bash
MAESTRO_IOS_SIMULATOR=iPhone 17
```

Each build runs `expo prebuild`, verifies local-network HTTP in `Info.plist`, and produces a Release simulator `.app`. Use `localhost` — not `10.0.2.2` — for iOS Simulator.

To target a specific simulator when running tests:

```bash
xcrun simctl list devices booted
export MAESTRO_DEVICE=<simulator-udid>
cd e2e/maestro && npm run test:e2e
```

## Setup

From `e2e/maestro`:

```bash
asdf install   # first time only
npm install
```

Install Maestro (macOS):

```bash
curl -Ls "https://get.maestro.mobile.dev" | bash
export PATH="$HOME/.maestro/bin:$PATH"
```

The test runner auto-detects Maestro at `~/.maestro/bin/maestro`. Override with `MAESTRO_BIN` if installed elsewhere.

## Test suites

| Suite | Command | Backend |
|-------|---------|---------|
| Mocked BE (CRUD + translation) | `npm run test:e2e` | WireMock stubs all API calls |
| Accessibility | `npm run test:accessibility` | WireMock |
| UAT smoke (create task) | `npm run test:uat` | Real running stack |
| All suites | `npm test` | Mixed |

Start WireMock for mocked-BE and accessibility runs:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-wiremock
```

Start the full stack for UAT:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d --build
```

If you ran mocked tests before UAT, restart WireMock so static validation mappings reload from `docker/docker-compose/mappings/`:

```bash
docker compose -f docker/docker-compose/run-application.yml restart qa-demo-wiremock
```

Run tests (with the matching app installed on a running emulator/simulator):

```bash
cd e2e/maestro
npm run test:e2e
npm run test:accessibility
npm run test:uat
```

## Architecture

```
e2e/maestro/
├── runSuite.ts              # CLI entry — parse suite arg, invoke runner
├── context/                 # TaskContext — randomised test data
├── data/                    # enums, DTOs, AllureEpic
├── interactions/
│   ├── pages/               # Maestro sub-flows — element waits only
│   ├── steps/               # workflow sub-flows
│   └── validators/          # assertion sub-flows
├── providers/               # SupportProvider (wiremock, mock, allure)
├── runner/
│   ├── MaestroTestRunner.ts # orchestrates mocks, Maestro CLI, Allure
│   ├── types.ts             # MaestroTestCase, TestSuite
│   ├── maestroTestCase.ts   # derives flow path from YAML sibling
│   └── testRegistry.ts      # auto-discovers tests/**/*.test.ts
├── support/
│   ├── allure/              # AllureClient, AllureTestReporter, maestroSteps
│   └── mocks/               # ApiRouteMock + WireMockClient
├── tests/                   # co-located *.test.ts + *.yaml flows only
└── scripts/                 # build/install scripts for Android and iOS
```

- **TypeScript runner** (`runSuite.ts` + `runner/`) sets up WireMock stubs, generates per-test env vars, and invokes Maestro.
- **New tests** — add a co-located `feature/feature.test.ts` + `feature.yaml` under `tests/`; the runner picks them up automatically (no registry edit).
- **Maestro YAML** sub-flows hold all `testID` selectors inline (page objects) — no compile-time link to the app.

## Reports

The TypeScript runner writes Allure results to `allure-results/`. Each test includes:

- **Setup WireMock stubs** — when the test uses mocked API responses
- **Maestro steps** — imported from Maestro `commands-*.json` after each flow run
  - Step names use `runFlow` **labels** when set, otherwise the sub-flow file name
  - **Parameters** on top-level Maestro steps show resolved test data (`TASK_TITLE`, `SCROLL_TARGET_ID`, etc.); nested steps omit them but still resolve env in step names
  - Failed steps attach the closest Maestro screenshot when available; failed/broken tests also attach a screenshot at test level

Use `label` on `runFlow` in YAML for readable step names in Allure:

```yaml
- runFlow:
    label: Open task info form
    file: ../../interactions/steps/open-task-info-form.yaml
    env:
      TASK_TITLE: ${TASK_TITLE}
```

Maestro artifacts for the latest run are stored under `.maestro-output/` (git-ignored).

The **Environment** section in Allure shows framework, suite, backend URL, app id, and device info (`platform`, `device`, `os_version`, `device_id`) parsed from Maestro's `Running on …` output during the test run.

```bash
npm run allure:serve
```

## CI

Android and iOS Maestro jobs run from `.github/workflows/react-native-e2e.yml`.

| Platform | Runner | Workflows |
|----------|--------|-----------|
| Android | `ubuntu-22.04` | `maestro-react-native-android-e2e.yml`, `maestro-react-native-android-uat.yml`, `maestro-react-native-android-accessibility.yml` |
| iOS | `macos-15-intel` | `maestro-react-native-ios-e2e.yml`, `maestro-react-native-ios-uat.yml`, `maestro-react-native-ios-accessibility.yml` |

iOS jobs use `run-maestro-ios-tests` (`build-ios-app.sh` → `install-ios-app.sh` → npm test). API URLs use `http://localhost:…` on the simulator. CocoaPods runs inside `build-ios-app.sh` via `npx pod-install` — no separate pipeline step.

Android CI invokes `run-android-maestro-ci.sh` as a single command inside `android-emulator-runner` (that action runs each line of a multiline `script:` in a separate shell, so shell variables do not persist across lines).

iOS CI uses `run-ios-maestro-ci.sh` (build → install → npm test). The Xcode build uses `generic/platform=iOS Simulator` so it does not require a booted device; install boots an iPhone simulator (defaults to `iPhone 16` on GitHub Actions).

GitHub-hosted macOS runners do not include Docker. iOS jobs use `macos-15-intel` so Colima can start a Linux VM (`start-e2e-env` runs `douglascamata/setup-docker-macos-action` on macOS). ARM runners (`macos-latest`) lack nested virtualization, so Colima cannot start there.
