# ADR 007: Unit vs. Integration Test Boundary Checklist

## Status
**Accepted** — September 8, 2026

## Context

"Testing Pyramid" and "Testing Trophy" are often debated as if one must be chosen wholesale. In practice, most drift toward a fake pyramid: tests labelled "unit" that actually mock every collaborator (including POJOs/DTOs) and assert on call sequences rather than output — expensive to maintain, break on harmless refactors, and erode trust in red CI until failures get ignored. The project needs an explicit boundary rule so this is decided by pattern, not by label or team preference.

## Decision

**A test's layer is decided by where the risk lives, not by the size of the unit under test.**

- **Unit test** when the code has high combinatorial complexity with no I/O — validation rules, pricing/state-machine logic, mappers with real logic, error branches cheap to trigger in isolation but expensive to provoke through a real system.
- **Integration test** when the risk lives at a boundary — DB queries, (de)serialization, HTTP status/error semantics, middleware, wiring between components. Public-facing integration tests remain the sole integration layer (ADR-004); this ADR governs what still belongs below that layer.
- **Existing tests are audited by assertion style, not by their current label:**

| Keep as unit | Fold into integration / fix |
|---|---|
| Asserts on produced state/output (Test Context Pattern, ADR-002) | Bare `verify(x).calledWith(...)` with nothing captured/asserted |
| Mocks only true external/infra boundaries | Mocks a POJO, DTO, or value object |
| Tests through the public API of the unit | Re-derives the expected value with the same algorithm as the code under test |

## Rationale

State-based assertions survive internal refactors; interaction-based assertions on non-boundary collaborators don't — and are usually redundant with what a public-facing integration test already covers. Auditing by pattern instead of layer name prevents both failure modes seen in real projects: pyramids that are secretly all-mocked integration tests, and trophy migrations that discard genuinely good unit tests just because "unit" is out of fashion.

The prerequisite for leaning on integration tests at all: they must be cheap enough to run on every change (Testcontainers-equivalent ephemeral infra, WireMock-equivalent third-party stubs, parallel CI — see ADR-004). Without that, shifting weight upward just trades low-confidence-but-fast tests for high-confidence-but-slow ones, recreating the trust problem from a different angle.

## Consequences

### Positive ✅
- Team has a checklist for code review instead of a subjective "does this feel like a unit test" debate
- Legacy suites can be migrated opportunistically (fix/fold when the file is already being touched) instead of a dedicated rewrite sprint
- Coverage % stops being treated as sufficient proof of confidence — mutation testing (PiTest/Stryker) is the escalation when coverage is high but trust is low

### Negative ⚠️
- Requires the integration-test infra prerequisite (ADR-004) to already be in place, or this decision pushes cost onto CI time instead of maintenance time
- Reclassifying existing tests is manual judgment, not a lint rule — mistakes will happen at the margins

## References
- [ADR 002](002-test-context-pattern-with-object-comparison.md) — state-based assertion pattern this checklist relies on
- [ADR 004](004-api-level-integration-tests-as-sole-integration-layer.md) — integration layer scope and infra prerequisite
- [ADR 006](006-90-percent-coverage-as-code-smell-detector.md) — why coverage % alone doesn't settle this
- **Rules:** `.cursor/rules/backend-testing.mdc`, `.cursor/rules/frontend-testing.mdc` — anti-pattern tables this ADR generalizes
