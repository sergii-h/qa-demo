# ADR 001: Use Vitest Instead of Jest

## Status
**Accepted** — December 24, 2025

## Context

The frontend needs a unit and integration test framework that supports shift-left testing and a ≥90% coverage target. The main options are **Jest** (ecosystem default) and **Vitest** (Vite-native, Jest-compatible API).

## Decision

Use **Vitest** for all frontend unit and integration tests.

## Rationale

- **Speed** — Vite-based runner; much faster cold start and watch mode than Jest at high test volume
- **Stack fit** — Native ESM and TypeScript; pairs with Vite 8, React 18, and Istanbul coverage already in the project
- **Low switching cost** — Jest-compatible API (`describe`, `it`, `expect`, `vi` instead of `jest`)
- **CI** — Shorter `demo-interface` pipeline runs on every push

**Not chosen:** Jest — mature and familiar, but slower and heavier to configure for this Vite + ESM stack.

## Consequences

### Positive ✅
- Faster local feedback and CI runs
- Less test-runner configuration
- Good fit for a modern demo stack

### Negative ⚠️
- Smaller community than Jest — mitigated by API compatibility with Jest docs
- Newer tool — may hit occasional edge cases

## Scope

- **In scope:** Frontend unit and integration tests (`demo-interface`)
- **Out of scope:** Backend (JUnit5), E2E (Playwright/Selenide), contracts (Pact)

## References

- [Vitest documentation](https://vitest.dev/)
- [Vitest vs Jest](https://vitest.dev/guide/comparisons.html)
- [Testing guide](../testing-guide.md)
- [Private testing rules](../../README.md#-documentation) — `.cursor/rules` submodule
