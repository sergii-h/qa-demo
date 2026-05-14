# ADR 004: Public-Facing Integration Tests as the Sole Integration Layer

## Status
**Accepted** - March 17, 2026

## Context

When writing integration tests, there is a temptation to add intermediate-level tests that exercise internal component combinations in isolation (e.g., controller + repository without HTTP, or a hook + child component without rendering the full parent). The question is whether these tests add value on top of unit tests and public-facing integration tests.

## Decision

**Public-facing integration tests are the sole integration layer.** No intermediate integration tests are added unless explicitly justified.

- **Backend:** integration tests hit the application through its public HTTP API using RestAssured against a full Spring Boot context with real infrastructure (MongoDB, Kafka) via Testcontainers
- **Frontend:** integration tests render the full component tree and interact with it as a user would (via React Testing Library), with only third-party dependencies mocked

## Rationale

Any internal path worth testing at integration level is reachable through the public interface:

```
Backend:  HTTP request → Controller → Repository → real MongoDB
Frontend: User interaction → Parent component → Child components → API calls
```

Adding a middle layer would test a strict subset of what the public-facing test already covers, with less realistic conditions.

**For complex internal logic** (e.g., repository queries, hooks): field permutations and edge cases belong at unit level. If they produce incorrect results, a public-facing integration test will catch it through the observable output.

## Consequences

### Positive ✅
- No redundant coverage between integration layers
- Integration tests always reflect real client usage
- Fewer tests to maintain without losing coverage

### Negative ⚠️
- Diagnosing failures requires reasoning about the full stack (mitigated by unit tests pinpointing the exact logic failure)

## Exception

An intermediate-level test is justified **only** if the internal logic requires so many data permutations that running them all through the full public interface would be too slow or noisy, **and** the behaviour cannot be reasonably verified through unit tests alone.

## References
- **Backend example:** `demo-service/src/test/java/com/example/demo/integration/test/api/CreateTaskTest.java`
- **Backend base:** `demo-service/src/test/java/com/example/demo/integration/ApiIntegrationTestBase.java`
- **Related:** ADR-003 (Test Plan Integration in User Stories)
