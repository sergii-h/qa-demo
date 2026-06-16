# Pact — Consumer-Driven Contract Tests

This project uses an **ephemeral Pact Broker** started fresh in Docker on each CI run. CI bootstraps `master` contracts first so `can-i-deploy` has a baseline to compare against.

> In production, a persistent broker (PactFlow or self-hosted) replaces the ephemeral approach and removes the bootstrap step.

## Consumers

| Workflow | Consumer | Provider surface |
|----------|----------|------------------|
| `pact-interface.yml` | `demo-interface` | Task HTTP API |
| `pact-notification.yml` | `notification-service` | Task events (async) |
| `pact-android.yml` | `demo-android` | Task HTTP API |

## Run the full pipeline locally

```bash
# All consumers → publish → provider verify → can-i-merge
bash .github/scripts/pact-run-local.sh

# Android-only pipeline
bash .github/scripts/pact-run-local-android.sh
```

## Run each phase manually

```bash
# 1) Start broker
cd demo-interface && npm run pact:broker:up

# 2) Frontend consumer contracts
npm run test:pact
PACT_CONSUMER_VERSION=$(git rev-parse --short HEAD) npm run pact:publish

# 3) Notification service consumer contract
cd ../notification-service && mvn test
# then publish via pact-cli docker image (see pact-run-local.sh for the full command)

# 4) Provider verification
cd ../demo-service
PACT_BROKER_BASE_URL=http://localhost:9292 \
  mvn verify -Pintegration-tests -Dit.test="*PactProviderTest" -Djacoco.skip=true

# 5) Can-i-merge gate
cd ../demo-interface
PACT_CONSUMER_VERSION=$(git rev-parse --short HEAD) npm run pact:can-i-deploy
```
