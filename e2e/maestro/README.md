# Maestro React Native E2E Tests

Black-box E2E tests for `demo-react-native` using [Maestro](https://maestro.mobile.dev/) on Android and iOS. Mirrors the page object → step → validator architecture used by Playwright and Android Compose E2E suites.

## Prerequisites & setup

- Node.js 22+ ([asdf](https://asdf-vm.com/) — `.tool-versions`)
- [Maestro CLI](https://maestro.mobile.dev/docs/getting-started/installing-maestro) — `curl -Ls "https://get.maestro.mobile.dev" | bash`; auto-detected at `~/.maestro/bin/maestro` (override with `MAESTRO_BIN`)
- **Android:** SDK (`platform-tools`, emulator or device). `$HOME/Library/Android/sdk` (macOS), `$HOME/Android/Sdk` (Linux), or `ANDROID_HOME` / `ANDROID_SDK_ROOT`
- **iOS (macOS only):** Xcode + iOS Simulator
- Docker for WireMock / UAT stack

From `e2e/maestro`:

```bash
asdf install   # first time only
npm install
```

## Configuration

Read from `.env.e2e`; override in `.env.e2e.local` (git-ignored).

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

Build scripts produce a **standalone release build** with an embedded JS bundle (no Metro). That differs from **`npx expo start`** (Metro / Expo Go). Always validate E2E against the Maestro-built app.

### Build & install

From `e2e/maestro` (scripts resolve paths from their own location — npm is recommended):

**Android** — start the emulator first:

```bash
npm run build:android:wiremock   # or build:android:uat
npm run install:android
```

**iOS** — boots a simulator automatically if none is running:

```bash
npm run build:ios:wiremock   # or build:ios:uat
npm run install:ios
```

Each build runs `expo prebuild`, verifies cleartext HTTP (Android manifest) or local-network HTTP (`Info.plist`), and auto-detects Android ABI (`arm64-v8a` / `x86_64`). iOS builds a Release simulator `.app` via `npx pod-install` (Expo; RN deprecation notice about raw `pod install` is safe to ignore).

Optional `.env.e2e.local` pins:

```bash
MAESTRO_IOS_SIMULATOR=iPhone 17
# or: MAESTRO_DEVICE=<udid>   # from: xcrun simctl list devices booted
```

## Test suites

| Suite | Command | Backend |
|-------|---------|---------|
| Mocked BE (CRUD + translation) | `npm run test:e2e` | WireMock stubs all API calls |
| Accessibility | `npm run test:accessibility` | WireMock |
| UAT smoke (create task) | `npm run test:uat` | Real running stack |
| All suites | `npm test` | Mixed |

WireMock (mocked BE / accessibility):

```bash
docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-wiremock
```

Full stack (UAT):

```bash
docker compose -f docker/docker-compose/run-application.yml up -d --build
```

If mocked tests ran before UAT, restart WireMock so mappings reload from `docker/docker-compose/mappings/`:

```bash
docker compose -f docker/docker-compose/run-application.yml restart qa-demo-wiremock
```

Run with the matching app installed on a running emulator/simulator:

```bash
cd e2e/maestro
npm run test:e2e            # or test:accessibility / test:uat
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

- **TypeScript runner** sets up WireMock stubs, generates per-test env vars, and invokes Maestro.
- **New tests** — add co-located `feature/feature.test.ts` + `feature.yaml` under `tests/`; auto-discovered (no registry edit).
- **Maestro YAML** sub-flows hold all `testID` selectors inline — no compile-time link to the app.

## Reports

Allure results → `allure-results/`; Maestro artifacts → `.maestro-output/` (git-ignored). `npm run allure:serve`.

Each test includes WireMock setup (when stubbed) and Maestro steps from `commands-*.json` — step names use `runFlow` **labels** when set, otherwise the sub-flow file name; top-level steps show resolved env parameters (`TASK_TITLE`, etc.); failures attach Maestro screenshots. The **Environment** section shows framework, suite, backend URL, app id, and device info. iOS details come from Maestro's `Running on …` line; Android resolves the connected emulator/device via `adb` (AVD name, model, OS/API level, serial) because Maestro often reports only `Running on test`.

```yaml
- runFlow:
    label: Open task info form
    file: ../../interactions/steps/open-task-info-form.yaml
    env:
      TASK_TITLE: ${TASK_TITLE}
```

On **push to `master`** and **pull requests**, reports publish to [GitHub Pages](https://sergii-h.github.io/qa-demo/) (`maestro-android-*`, `maestro-ios-*`). iOS UAT is listed but not published — see CI below.

## CI

Android and iOS Maestro jobs run from `.github/workflows/react-native-e2e.yml`.

| Platform | Runner | Workflows |
|----------|--------|-----------|
| Android | `ubuntu-22.04` | `maestro-react-native-android-e2e.yml`, `maestro-react-native-android-accessibility.yml`, `maestro-react-native-android-uat.yml` |
| iOS | `macos-15` | `maestro-react-native-ios-e2e.yml`, `maestro-react-native-ios-accessibility.yml` |

**iOS** — `run-ios-maestro-ci.sh` (build → install → npm test). WireMock via standalone Java JAR (`wiremock-backend:standalone`) — no Docker. **UAT is not run in CI**: GitHub-hosted macOS ARM runners lack Apple virtualization, so Colima/Docker cannot start — run locally (`npm run build:ios:uat`, full stack on `localhost:8080`). Boots **iPhone 16 on iOS 18.x**, passes `platform=iOS Simulator,id=…` to `xcodebuild` (generic `arch=` fails with multiple runtimes); `MAESTRO_DEVICE` set from the same UDID.

**Android** — `run-android-maestro-ci.sh` inside `android-emulator-runner` (multiline `script:` runs each line in a separate shell — variables do not persist). **UAT** uses the full Docker stack on `ubuntu-22.04`.

All iOS jobs use **`macos-15` (Apple Silicon)** with `arm64` simulator builds. API URLs use `http://localhost:…` on the simulator.
