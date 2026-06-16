# demo-interface

React 18 · TypeScript 5 · Vite 8 · PrimeReact 10 · Node 22

Frontend for the Task Management app. Talks to `demo-service` via the REST API.

## Prerequisites

- Node 22 (≥22.12.0)

## Running Tests

### Unit + integration tests + coverage (Istanbul ≥90%)

```bash
npm test
open coverage/index.html
```

### Interactive Vitest UI

```bash
npm run test:ui
```

### Mutation tests (Stryker — on-demand)

Stryker is configured (`stryker.config.json`) but excluded from the regular CI run — it is slow and resource-intensive. Run manually when validating assertion quality.

```bash
npm run test:stryker
```

### Pact consumer contracts

Run as part of the full Pact pipeline — see [doc/pact.md](../doc/pact.md).

```bash
npm run test:pact
PACT_CONSUMER_VERSION=$(git rev-parse --short HEAD) npm run pact:publish
```
