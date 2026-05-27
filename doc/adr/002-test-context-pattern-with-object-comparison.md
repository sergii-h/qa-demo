# ADR 002: Test Context Pattern with Object Comparison

## Status
**Accepted** — January 3, 2026 (updated January 26, 2026)

## Context

Unit and integration tests often need to assert multi-field domain objects. Field-by-field assertions are verbose, easy to incomplete, and costly to maintain when models grow.

## Decision

Use **test context classes** (builder-based defaults + factory methods) and validate **complete objects** with one equality assertion.

**Pattern:**

1. Build a `TaskTestContext` with the business data for the scenario  
2. Run the code under test  
3. Copy **system-generated** fields from the result (`id`, timestamps) onto the context  
4. Assert `assertThat(actual, is(context.createTask()))`

```java
context.setId(capturedTask.getId());
context.setCreatedDate(capturedTask.getCreatedDate());
context.setUpdatedDate(capturedTask.getUpdatedDate());
assertThat(capturedTask, is(context.createTask()));
```

**Integration tests:** after each API call, sync server fields with `updateFromResponse(response)` before asserting.

**Rule:** mutate only dynamic/system fields — never business data on the context after the scenario is defined.

## Rationale

- **Complete validation** — new model fields break tests until the context is updated  
- **Less noise** — one assertion instead of many `assertEquals`  
- **Single source of truth** — defaults and factories live in one class  
- **Clear failures** — Hamcrest `assertThat(actual, is(expected))` (and similar matchers) report mismatches in a structured way; domain types use Lombok `@Value` so equality is well defined

**Not chosen:** field-by-field asserts (incomplete, repetitive); `ignoringFields()` (drops the safety net); passing 10+ constructor/factory args per test (unreadable for larger DTOs).

## When to use

- **Use:** objects with **3+ fields**, full-object validation, shared test data across tests  
- **Skip:** 1–2 field values, one-off data, or intentional partial assertions

## Consequences

### Positive ✅
- Consistent pattern in unit tests (`TaskControllerTest`) and integration tests (`CreateTaskTest`, etc.)
- Scales as the model grows without growing every test method

### Negative ⚠️
- One-time cost to maintain context classes  
- Mutable context — use a fresh or clearly scoped context per test; not for parallel sharing  
- Whole-object asserts only pay off with **good failure output** — use Hamcrest/AssertJ-style matchers, not bare `==` or opaque custom checks

## References

- **Context:** `demo-service/src/test/java/com/example/demo/context/TaskTestContext.java`
- **Unit example:** `demo-service/src/test/java/com/example/demo/TaskControllerTest.java`
- **Integration example:** `demo-service/src/test/java/com/example/demo/integration/test/api/CreateTaskTest.java`
- [Testing guide](../testing-guide.md)
- [Private testing rules](../../README.md#-documentation) — `.cursor/rules` submodule
- [Test Data Builder (Martin Fowler)](https://martinfowler.com/bliki/ObjectMother.html)
