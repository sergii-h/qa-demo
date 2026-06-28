# ADR 006: 90% Coverage Threshold as a Code-Smell Detector

## Status
**Accepted** — June 24, 2026

## Context

High coverage targets are criticised for producing test theater — tests written to satisfy the number rather than verify behaviour. Despite this, the project enforces 90%+ as a hard build gate.

## Decision

**90%+ line coverage is a build gate, not a quality metric.**

The threshold is set higher than the industry default deliberately. A failing gate is a prompt to ask *why* a line is hard to reach — not a signal to write more tests.

## Rationale

When a line is hard to reach, the cause is almost always a production code smell:

- **Inline anonymous callback** with no reachable entry point → extract to a named function/constant
- **Hidden default dependency** that cannot be swapped in tests → remove the default, always inject
- **Mixed-responsibility function** where one branch belongs elsewhere → split the function

A lower threshold (80%) or informational-only coverage lets these smells go unnoticed. The friction of the gate is intentional — it forces the fix at the moment the smell appears.

## Consequences

### Positive ✅
- Hard-to-test logic is extracted early, improving production code clarity
- The gate applies the same pressure to developers and AI models

### Negative ⚠️
- Risk of gaming the metric with trivial tests — mitigated by the companion rule against testing constants, getters, and barrel files
- Genuinely unreachable lines (defensive branches by design) require an explicit exclusion annotation

## References

- **Rule:** `.cursor/rules/common-testing.mdc` — coverage gate and testability-driven refactoring rule
- **Example:** `demo-react-native/src/components/EnumPicker.tsx` — `openMenu` extracted from inline `onPressIn` callback to make the handler reachable in tests
- [ADR 003](003-test-plan-integration-in-user-stories.md) — shift-left test planning per story
- [ADR 004](004-api-level-integration-tests-as-sole-integration-layer.md) — integration test scope
