# QA Demo — Android App

Native Android client for the QA Demo task management API. Same CRUD functionality as the React web app, using the existing Spring Boot backend.

**Requirements:** [Frontend requirements](../doc/requirements/front-end/README.md)

## Stack

- Kotlin · Jetpack Compose · Material 3
- Retrofit + Moshi · ViewModel + StateFlow
- Min SDK 26 · Target SDK 35

## Localization

- UI strings: `app/src/main/res/values/strings.xml` (English) and `values-es/strings.xml` (Spanish)
- **EN / ES** switcher on the task list screen (top bar); choice is persisted across restarts
- On first launch, Spanish is used when the device language is `es` / `es-*`; otherwise English (same rule as the web app)

## Features

| Screen | API |
|--------|-----|
| Task list | `GET /v1/tasks` |
| Create task | `POST /v1/tasks` |
| Edit task | `PUT /v1/tasks/{id}` |
| Delete task | `DELETE /v1/tasks/{id}` |
| Task info (+ validation) | `GET /v1/tasks/{id}`, `GET /v1/tasks/isValid/{id}` |

## Prerequisites

1. **Android Studio** (latest stable) with SDK 35 and an emulator or physical device
2. **Backend running** — same as the web app:

```bash
docker compose -f docker/docker-compose/run-application.yml up -d qa-demo-mongo qa-demo-kafka qa-demo-wiremock
cd demo-service && mvn spring-boot:run
```

## Open in Android Studio

1. **File → Open** → select the `demo-android` folder
2. Wait for Gradle sync to finish
3. Create/start an emulator (or connect a device)
4. Click **Run**

## API base URL

Default (Android emulator → host machine):

```properties
http://10.0.2.2:8080/v1/
```

Override in `demo-android/local.properties` (create this file — it is gitignored):

```properties
sdk.dir=/Users/you/Library/Android/sdk
api.base.url=http://10.0.2.2:8080/v1/
```

| Environment | URL |
|-------------|-----|
| Emulator | `http://10.0.2.2:8080/v1/` |
| Physical device (same Wi‑Fi) | `http://<your-mac-ip>:8080/v1/` |

After changing `api.base.url`, rebuild the app.

## Build from CLI

Gradle needs **JDK 17+** on your `PATH` (Android Studio’s bundled JBR is fine). If `java` is not found in the terminal, point `JAVA_HOME` at Android Studio’s runtime:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
export PATH="$JAVA_HOME/bin:$PATH"
```

```bash
cd demo-android
./gradlew test                  # JVM unit tests (no device)
./gradlew assembleDebug
./gradlew installDebug          # device/emulator connected
```

## Unit tests

- Location: `app/src/test/`
- Stack: JUnit 4, MockK, Truth, Robolectric, coroutines-test, Compose UI Test (`createComposeRule` in `src/test` — no emulator)
- Mutation testing: not used — Android PiTest support is experimental and a poor fit for Robolectric/Compose; Kover line coverage is the quality gate instead (same rationale as Stryker on the web app).

### Coverage

[Kover](https://github.com/Kotlin/kotlinx-kover) on JVM unit tests. `./gradlew test` runs tests and enforces thresholds (90% line, instruction, and branch; 100% per-class line coverage).

Tests + HTML report:

```bash
./gradlew :app:koverHtmlReportDebug
open app/build/reports/kover/htmlDebug/index.html
```

Kover excludes Compose compiler-generated classes (`*Kt$*`, `*ComposableSingletons*`); screen and composable coverage stays in the report.

### Gradle daemon JVM error

If you see `No defined toolchain download url for MAC_OS on aarch64` with `vendor=JetBrains`, remove `gradle/gradle-daemon-jvm.properties` (or regenerate it with `./gradlew updateDaemonJvm` after JDK 17+ is installed). That file pins the Gradle daemon to a JetBrains JDK without download URLs for Apple Silicon.

## Project layout

```
demo-android/
├── app/src/main/java/com/example/demo/
│   ├── data/           # Models + Retrofit API
│   ├── repository/     # TaskRepository
│   └── ui/             # Compose screens + navigation
└── README.md
```

## Notes

- Cleartext HTTP is allowed for local development (`network_security_config.xml`)
- For production, use HTTPS and restrict cleartext traffic
