# ADR 005: Domain-Grouped Step and Validation Providers for E2E

## Status
**Accepted** — June 9, 2026

## Context

E2E tests in this repo are implemented on multiple stacks. **Current:** Playwright (TypeScript), Selenide (Java), and Android Compose (Kotlin). **More runners are expected** (e.g. iOS, additional web frameworks) as the demo grows.

All E2E stacks share the same scenarios (mocked backend, accessibility, UAT) and the same vocabulary: open a form, act on it, assert on the UI. New stacks should follow the same provider pattern so structure stays comparable across platforms.

Without a consistent entry point, tests tend to wire step and validator classes ad hoc — on a base class, as loose fields, or via nested objects with inconsistent naming. That works at demo size but does not scale or onboard well when many feature areas exist.

## Decision

Use **domain-grouped providers** as the composition root for E2E interactions and assertions:

- **`StepProvider`** — exposes feature-area step objects (`tasks`, `createTask`, `language`, …)
- **`ValidationProvider`** — exposes feature-area validators (`tasks`, `task`, `language`, …)

Tests depend on **`steps.*`** and **`validate.*`**, not on individual step/validator classes or a growing base class.

**Pattern:**

```java
// Selenide / Android (JVM — fluent chaining)
steps.tasks
    .openCreateTaskForm()
    .setTaskData(context)
    .submitForm();
validate.tasks.hasTask(context);
```

```typescript
// Playwright (async — separate awaits)
await step.tasks.openCreateTaskForm();
await step.tasks.createTask.fillForm(context);
await step.tasks.createTask.submitForm();
await validate.tasks.hasTask(context);
```

**Adding a feature area:** implement a step/validator class → register one property on each provider → use from tests. Base classes and fixtures wire providers once; they do not accumulate feature fields.

**Domain grouping:** provider properties map to product areas (`tasks`, `createTask`, `language`), not to a flat list of every test helper. At larger scale, split into nested or suite-specific providers (e.g. `billing.invoice`) rather than a single object with dozens of top-level properties.

## Rationale

- **Cross-stack parity** — same mental model across every E2E runner in the repo; demo doubles as a reference for production E2E frameworks
- **Discoverability** — IDE autocomplete on `steps.` / `validate.` lists available domains
- **Thin bases / fixtures** — `@Before` or Playwright fixtures construct providers; test classes stay focused on scenario logic
- **Extension point** — new feature = new step + one provider line; no base-class surgery

**Not chosen:**

| Alternative | Why not |
|-------------|---------|
| Wire step/validator fields directly on base classes | Base grows with every feature; inconsistent naming (`steps` vs `languageSteps`) |
| Flat “god” provider (100+ top-level properties) | Same maintenance problem, without domain structure |
| Construct steps locally in each test | Duplicated wiring; no shared vocabulary across teams |
| Identical call style on all stacks | Playwright needs `await` per step; JVM stacks use fluent return types instead |

## Stack notes

Each E2E runner wires providers in its own way; chaining style follows the language (async vs fluent). **New stacks must adopt the same provider shape** — domain-grouped `StepProvider` and `ValidationProvider` — even if injection and syntax differ.

| Stack (current) | Provider wiring | Step chaining |
|-----------------|-----------------|---------------|
| Playwright | `fixtures/actions.ts` injects `step`, `validate` | Separate `await` calls (async) |
| Selenide | `StepProvider` / `ValidationProvider` on spec interfaces | Fluent — `openCreateTaskForm()` returns form step |
| Android | `StepProvider` / `ValidationProvider` in E2E base `@Before` | Fluent — same as Selenide |

Co-locate accessibility and UAT tests with feature folders under `test/` (e.g. `test/create/`); providers and support code remain shared under `provider/`, `interaction/`, and `support/`.

## When to use

- **Use:** all E2E tests that interact with the UI through shared step/validator classes, on any stack in the repo
- **New stack:** add `StepProvider` / `ValidationProvider` with the same domain properties as existing runners; register in that stack’s fixture or base class
- **Evolve:** when feature count makes a single provider unwieldy, introduce domain-scoped providers or nested grouping — do not remove the provider pattern

## Consequences

### Positive ✅
- Consistent test vocabulary across all E2E stacks — current and future
- Clear place to register new feature areas
- Documents production-relevant structure even at demo scale

### Negative ⚠️
- One extra indirection layer (`steps.tasks` vs calling `TaskTableSteps` directly) — acceptable trade-off for consistency and scale
- Playwright and JVM stacks differ in chaining style by necessity (async vs sync)

## References

- **Playwright:** `e2e/playwright-typescript/providers/StepProvider.ts`, `ValidationProvider.ts`; fixtures in `fixtures/actions.ts`
- **Selenide:** `e2e/selenide-junit5-selenium-grid/src/test/java/provider/StepProvider.java`, `ValidationProvider.java`
- **Android:** `demo-android/app/src/androidTest/java/com/example/demo/e2e/provider/`, `interaction/`, `support/`, `test/`
- **Example tests:** `e2e/selenide-junit5-selenium-grid/src/test/java/test/spec/createTask/ICreateTaskTest.java`, `demo-android/.../e2e/test/create/CreateTaskTest.kt`, `e2e/playwright-typescript/tests/create-task/create-task.spec.ts`
- [ADR 002](002-test-context-pattern-with-object-comparison.md) — test data context pattern (complements providers; `TaskContext` / `TaskTestContext` feed steps and validators)
- [Testing guide](../testing-guide.md)
