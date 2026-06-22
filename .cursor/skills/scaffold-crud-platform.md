---
name: scaffold-crud-platform
description: >-
  Scaffolds a complete new CRUD platform in qa-demo following existing
  conventions (layers, tests, CI). Use when the user asks to add a new app,
  client, platform, scaffold a CRUD app, or create a new backend service,
  web client, or mobile app in this project.
---

# Scaffold CRUD Platform

Generates a new CRUD platform in qa-demo mirroring the architecture,
testing pyramid, and CI conventions of the three reference platforms.

## Step 1 — Identify platform type

| Platform type | Reference platform | Key tech |
|---|---|---|
| Backend service (JVM) | `demo-service` | Spring Boot, MongoDB, Kafka |
| Web SPA | `demo-interface` | React, Vite, TypeScript |
| Android mobile | `demo-android` | Kotlin, Jetpack Compose |
| Other | Ask user | — |

Name the new directory `demo-<platform-name>` at repo root.

## Step 2 — Read reference files first

Before writing any code, read these files from the reference platform.
They are the source of truth for patterns — never invent structure.

### Backend reference reads (`demo-service`)

Read the rule file first: `.cursor/rules/backend-testing.mdc`

```
demo-service/src/main/java/com/example/demo/task/TaskController.java
demo-service/src/main/java/com/example/demo/task/TaskService.java
demo-service/src/main/java/com/example/demo/task/TaskRepository.java
demo-service/src/main/java/com/example/demo/task/Task.java
demo-service/src/test/java/com/example/demo/task/TaskControllerTest.java
demo-service/src/test/java/com/example/demo/integration/test/api/TaskControllerIntegrationTest.java
demo-service/src/test/java/com/example/demo/integration/test/pact/api/TasksProviderPactTest.java
.github/workflows/demo-service.yml
```

### Web reference reads (`demo-interface`)

Read the rule file first: `.cursor/rules/frontend-testing.mdc`

```
demo-interface/src/interfaces/ITask.ts
demo-interface/src/services/index.ts
demo-interface/src/components/tasksTable/tasksTable.tsx
demo-interface/src/components/tasksTable/tasksTable.unit.test.tsx
demo-interface/src/components/tasksTable/tasksTable.integration.test.tsx
demo-interface/src/test-utils/mockFetch.ts
demo-interface/src/test/pact/tasks.pact.fixtures.ts
demo-interface/src/test/pact/tasks.get-all.pact.test.ts
.github/workflows/demo-interface.yml
```

### Android reference reads (`demo-android`)

Read the rule file first: `.cursor/rules/android-testing.mdc`

```
demo-android/app/src/main/java/com/example/demo/data/model/Task.kt
demo-android/app/src/main/java/com/example/demo/data/remote/ApiClient.kt
demo-android/app/src/main/java/com/example/demo/data/remote/TaskApi.kt
demo-android/app/src/main/java/com/example/demo/repository/TaskRepository.kt
demo-android/app/src/main/java/com/example/demo/ui/tasklist/TaskListViewModel.kt
demo-android/app/src/main/java/com/example/demo/ui/tasklist/TaskListScreen.kt
demo-android/app/src/main/java/com/example/demo/ui/TestTags.kt
demo-android/app/src/test/java/com/example/demo/testing/TaskFixtures.kt
demo-android/app/src/test/java/com/example/demo/ui/tasklist/TaskListViewModelTest.kt
demo-android/app/src/test/java/com/example/demo/integration/support/IntegrationTestBase.kt
demo-android/app/src/test/java/com/example/demo/integration/TaskListIntegrationTest.kt
demo-android/app/src/test/java/com/example/demo/pact/TasksGetAllPactTest.kt
demo-android/app/src/androidTest/java/com/example/demo/e2e/test/create/CreateTaskTest.kt
.github/workflows/demo-android.yml
```

## Step 3 — Create layers in order

### Backend — 4 layers (bottom → top)

1. **`domain/model/<Entity>.java`** — entity, request DTO, enums  
   Template: `demo-service/.../task/Task.java`

2. **`domain/repository/<Entity>Repository.java`** — Spring Data interface  
   Template: `demo-service/.../task/TaskRepository.java`

3. **`domain/service/<Entity>Service.java`** — business logic, validation, event publishing  
   Template: `demo-service/.../task/TaskService.java`

4. **`api/controller/<Entity>Controller.java`** — REST endpoints, DTOs, error handling  
   Template: `demo-service/.../task/TaskController.java`

### Web — 3 layers

1. **`src/interfaces/I<Entity>.ts`** — interfaces and enums
2. **`src/services/index.ts`** — fetch functions, one per CRUD operation
3. **`src/components/<feature>/`** — one folder per feature (table, createModal, editModal, infoModal)  
   Each folder: `<feature>.tsx` + `index.ts` barrel + unit + integration + translation test files

### Android — 5 layers (bottom → top)

1. **`data/model/<Entity>.kt`** + enums + `<Entity>Request` DTO  
   Template: `demo-android/.../data/model/Task.kt`

2. **`data/remote/<Entity>Api.kt`** — Retrofit interface, one method per CRUD + `isValid`  
   Template: `demo-android/.../data/remote/TaskApi.kt`

3. **`repository/<Entity>Repository.kt`** — thin delegate (mock boundary for ViewModels)  
   Template: `demo-android/.../repository/TaskRepository.kt`

4. **`ui/<entity>list/<Entity>ListViewModel.kt`** etc. — state + coroutines  
   Template: `demo-android/.../ui/tasklist/TaskListViewModel.kt`

5. **`ui/<entity>list/<Entity>ListScreen.kt`** etc. — Compose UI, consumes ViewModel only  
   Template: `demo-android/.../ui/tasklist/TaskListScreen.kt`

Also create: `ui/TestTags.kt`, `ui/i18n/ErrorMessages.kt`, `ui/i18n/TaskLabels.kt`,
`locale/LocalizedContent.kt`, `navigation/`, test fixtures and test support classes.

## Step 4 — Tests at each layer

For all naming and structure conventions see `common-testing.mdc`.

### Backend test boundaries

| Layer | Framework | What to mock |
|---|---|---|
| Service unit | JUnit5 + Mockito | `Repository` + `EventProducer` |
| Controller unit | JUnit5 + Mockito + MockMvc | `Service` |
| Integration | `@SpringBootTest` + Testcontainers (MongoDB, Kafka) + REST Assured | nothing — real stack |
| Pact provider | Spring provider test | real app against Pact broker |

**Coverage gate:** 90% — JaCoCo in `pom.xml`.

### Web test boundaries

| Layer | Test suffix | What to mock |
|---|---|---|
| `services` | `.unit.test.ts` | `globalThis.fetch` |
| Component unit | `.unit.test.tsx` | child modals + `vi.spyOn(services, ...)` |
| Component integration | `.integration.test.tsx` | only `fetch` via `mockFetchResponse` |
| Pact consumer | `src/test/pact/<entity>.<verb>.pact.test.ts` | Pact mock server (real service fn) |

**Coverage gate:** 90% — Vitest + Istanbul in `vitest.config.mts`.

### Android test boundaries

| Layer | Framework | What to mock |
|---|---|---|
| ViewModel unit | JUnit4 + MockK + Robolectric + `MainDispatcherRule` | `Repository` |
| Screen unit | JUnit4 + Compose test + Robolectric | `ViewModel` (indirect — via Repository) |
| Integration | JUnit4 + Robolectric + `MockWebServer` + `IntegrationTestBase` | nothing — real Compose stack |
| Pact consumer | JUnit5 + Pact JVM | Pact mock server (real `TaskApi` runs against it) |
| E2E | Instrumented + WireMock + `MockedBackendTestBase` | WireMock-backed BE |

**Coverage gate:** 90% — Kover in `build.gradle.kts`.

## Step 5 — CI workflow

Add `.github/workflows/demo-<platform>.yml`.

Key rules:
- Path-filter to `demo-<platform>/**` and the workflow file itself
- `ubuntu-22.04` runner
- Audit action: `audit-npm` (web), `audit-osv` (Android/JVM)
- Tests must enforce the 90% coverage gate before passing
- Pact consumer gets a separate `pact-<platform>.yml` workflow

### CI workflow template — `demo-<platform>.yml`

```yaml
name: Run <platform> tests

on:
  push:
    branches: [master]
    paths:
      - 'demo-<platform>/**'
      - '.github/workflows/demo-<platform>.yml'
  pull_request:
    branches: [master]
    paths:
      - 'demo-<platform>/**'
      - '.github/workflows/demo-<platform>.yml'

jobs:
  run-tests:
    runs-on: ubuntu-22.04
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4          # swap for setup-java or Android SDK as needed
        with:
          node-version: '22'
          cache: npm
          cache-dependency-path: demo-<platform>/package-lock.json
      - name: Install dependencies
        working-directory: demo-<platform>
        run: npm ci
      - name: Audit dependencies
        uses: ./.github/actions/audit-npm    # use audit-osv for JVM/Android
        with:
          working-directory: demo-<platform>
      - name: Run tests
        working-directory: demo-<platform>
        run: npm test
```

### Pact consumer workflow — `pact-<platform>.yml`

Needed for web and Android platforms. Model from `.github/workflows/pact-interface.yml` or `pact-android.yml`.

Key steps:
1. Start Pact broker: `docker compose -f docker/docker-compose/run-pact-broker.yml up -d`
2. Bootstrap master contracts on feature branches
3. `npm run test:pact` (or Gradle pact task) + publish to broker
4. Provider verification via Maven (`demo-service`)
5. `can-i-merge` gate on non-master branches

## Step 6 — Wire README and project config

- Add `demo-<platform>` entry to repo root `README.md`
- Add a `demo-<platform>/README.md` following the pattern in `demo-android/README.md`

---

## Domain model

All three platforms share the same backend contract.

### Task entity

| Field | Type | Notes |
|---|---|---|
| `id` | `string` / `String` | MongoDB ObjectId (`^[a-f0-9]{24}$`) |
| `title` | `string` / `String` | Required |
| `description` | `string \| null` | Optional |
| `status` | `TaskStatus` enum | `TODO \| IN_PROGRESS \| DONE` |
| `priority` | `TaskPriority` enum | `LOW \| MEDIUM \| HIGH` |
| `createdDate` | `string \| null` | ISO timestamp |
| `updatedDate` | `string \| null` | ISO timestamp |

### REST API endpoints

| Method | Path | Purpose |
|---|---|---|
| GET | `/v1/tasks` | List all |
| GET | `/v1/tasks/:id` | Get by ID |
| GET | `/v1/tasks/isValid/:id` | Validation check |
| POST | `/v1/tasks` | Create |
| PUT | `/v1/tasks/:id` | Update |
| DELETE | `/v1/tasks/:id` | Delete (204 No Content) |

---

## Naming conventions

| Concern | Backend (Java) | Web (TypeScript) | Android (Kotlin) |
|---|---|---|---|
| Test suffix — unit | `*Test.java` | `*.unit.test.ts(x)` | `*Test.kt` |
| Test suffix — integration | `*IntegrationTest.java` | `*.integration.test.tsx` | `*IntegrationTest.kt` |
| Test suffix — pact | `*PactTest.java` | `src/test/pact/*.pact.test.ts` | `*PactTest.kt` |
| Test naming | `should<Behavior>When<Condition>` | same | same |
| Fixture class | `TaskFixture` / test data builders | inline `mock<Entity>` | `TaskFixtures.kt` |
| Test IDs | N/A | inline strings or `testTags.ts` | centralized `TestTags.kt` |

---

## Backend patterns (demo-service)

### Layer creation order

```
Task.java (entity + enums)
  → TaskRequest.java (write DTO)
  → TaskRepository.java (Spring Data interface)
  → TaskService.java (business logic + event pub)
  → TaskController.java (REST, validation, error mapping)
```

### Unit test mock boundaries

- `TaskServiceTest` — mocks `TaskRepository` + `TaskEventProducer`
- `TaskControllerTest` — mocks `TaskService`; uses `@WebMvcTest` / MockMvc

### Integration test stack

- `@SpringBootTest` + `@AutoConfigureRestAssured`
- Testcontainers: `MongoDBContainer` + `KafkaContainer`
- No mocks — full Spring context + real persistence

### Pact provider pattern

- Provider test extends `AbstractPactTest`
- `@Provider("demo-service-tasks-<verb>")` per interaction
- Real Spring context; stubs pre-populated state via `@State` methods

---

## Web patterns (demo-interface)

### Unit vs integration test distinction

| Aspect | Unit | Integration |
|---|---|---|
| Mocks | Child modals + `vi.spyOn(services, ...)` | Only `fetch` via `mockFetchResponse` |
| Scope | Single component + props | Full component tree + real service calls |
| Assertions | `getByTestId`, spy call counts | `userEvent.click`, `waitFor`, fetch URL checks |

### Pact consumer pattern

- One file per endpoint: `tasks.<verb>.pact.test.ts`
- `createPact(providerName)` factory in `tasks.pact.fixtures.ts`
- Inside `executeTest`: call the **real service function** with `mockServer.url`
- Use matchers: `like()`, `regex()`, `eachLike()` — never exact values for IDs/timestamps

---

## Android patterns (demo-android)

### Layer creation order

```
data/model/Task.kt (entity + enums + TaskRequest + ErrorResponse + TaskValidation)
  → data/remote/ErrorBodyParser.kt (standalone parse fn — NOT inside ApiClient)
  → data/remote/ApiClient.kt
  → data/remote/TaskApi.kt (Retrofit interface)
  → repository/TaskRepository.kt (no default constructor param — always inject API)
  → ui/i18n/ErrorMessages.kt (imports data layer only — never ApiClient directly)
  → ui/i18n/TaskLabels.kt + locale/LocalizedContent.kt
  → ui/<entity>/ViewModels + Screens
  → ui/TestTags.kt (all testIDs as constants — never inline strings in tests)
  → test/testing/TaskFixtures.kt + ComposeTestSupport.kt
  → test/integration/support/IntegrationTestBase.kt + MockWebServer setup
```

### Key non-obvious conventions

- **Integration tests use `MockWebServer`** via `IntegrationMockServer` scripting helpers (`enqueueGetTasks`, `enqueueCreateTask`, etc.). Mock only the HTTP layer, not internal interfaces like `TaskApi` or `TaskRepository`; this keeps Retrofit, OkHttp, and Moshi serialization real.
- **`TestTags`** is a centralized object — add every new testID there, never as an inline string in a test.
- **Pact error contracts** — use `runCatching { }.exceptionOrNull()` + `assertThat(thrown).isInstanceOf(...)`. No try/catch in tests.
- **`ErrorMessages.kt`** must import from `data.remote.parseErrorBody`, not from `ApiClient`. Keep the error-body parsing out of the UI layer.

---

## Checklist — new platform is ready

- [ ] Directory `demo-<platform>/` created at repo root
- [ ] All layers created bottom-up following the reference order
- [ ] Every source file has a corresponding test file
- [ ] Coverage gate configured at 90%
- [ ] `TestTags.kt` (Android) or equivalent with all testID constants
- [ ] i18n locale keys added (`en/` and `es/`)
- [ ] `.github/workflows/demo-<platform>.yml` added
- [ ] Pact consumer workflow added (web/Android)
- [ ] Entry added to repo root `README.md`
- [ ] `demo-<platform>/README.md` created following `demo-android/README.md` pattern
- [ ] All tests pass locally
