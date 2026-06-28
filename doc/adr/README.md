# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records (ADRs) for the QA Demo project. ADRs document significant architectural and technical decisions, including context, alternatives considered, and rationale.

---

## What is an ADR?

An Architecture Decision Record (ADR) captures an important architectural decision made along with its context and consequences. ADRs help team members understand why certain decisions were made and provide a historical record of the project's evolution.

### ADR Format

Each ADR follows this structure:
- **Status:** Proposed | Accepted | Deprecated | Superseded
- **Date:** When the decision was made
- **Context:** The problem and constraints
- **Decision Drivers:** Factors influencing the decision
- **Considered Options:** Alternatives evaluated
- **Decision Outcome:** The chosen option and rationale
- **Consequences:** Positive and negative impacts

---

## ADR Index

### Testing & Quality

| ID | Title | Status | Date | Summary |
|----|-------|--------|------|---------|
| [001](001-vitest-over-jest.md) | Use Vitest Instead of Jest | Accepted | 2025-12-24 | Choose Vitest for frontend unit/integration tests due to performance, modern stack compatibility, and shift-left enablement |
| [002](002-test-context-pattern-with-object-comparison.md) | Test Context Pattern with Object Comparison | Accepted | 2026-01-03 | Builder-based test contexts; sync dynamic fields; assert full objects instead of field-by-field checks |
| [003](003-test-plan-integration-in-user-stories.md) | Test Plan Integration in User Stories | Accepted | 2026-02-17 | Integrate explicit test plans into user stories specifying UT/IT/Pact/E2E/Accessibility/UAT levels. Follows testing pyramid and shift-left principles with clear test ownership per story |
| [004](004-api-level-integration-tests-as-sole-integration-layer.md) | Public-Facing Integration Tests as Sole Integration Layer | Accepted | 2026-03-17 | Integration tests only through public HTTP API (BE) and full component tree (FE); no intermediate integration layers unless explicitly justified |
| [005](005-domain-grouped-step-and-validation-providers-for-e2e.md) | Domain-Grouped Step and Validation Providers for E2E | Accepted | 2026-06-09 | StepProvider and ValidationProvider as domain-grouped composition roots; consistent E2E vocabulary across Playwright, Selenide, and Android |
| [006](006-90-percent-coverage-as-code-smell-detector.md) | 90% Coverage Threshold as a Code-Smell Detector | Accepted | 2026-06-24 | 90%+ line coverage enforced as a build gate not for quality metrics, but as a forcing function to surface inline logic, hidden dependencies, and mixed responsibilities that need refactoring |

---

## Creating a New ADR

When making a significant technical decision:

1. **Create a new file:** `doc/adr/XXX-title-in-kebab-case.md`
2. **Use next number:** Increment from the last ADR (e.g., 002, 003)
3. **Follow the template:** See [001-vitest-over-jest.md](001-vitest-over-jest.md) as reference
4. **Update this index:** Add entry to the table above
5. **Commit with team:** Discuss with stakeholders before accepting

---

## ADR Lifecycle

- **Proposed** - Decision is being evaluated
- **Accepted** - Decision is approved and implemented
- **Deprecated** - Decision is no longer recommended but still in use
- **Superseded** - Decision has been replaced (link to new ADR)

---

## References

- [ADR GitHub Organization](https://adr.github.io/) - ADR best practices
- [Michael Nygard's ADR Template](https://cognitect.com/blog/2011/11/15/documenting-architecture-decisions)

---

**Note:** ADRs are living documents. They can be updated with new information, but decisions should not be changed without creating a new ADR.
