# demo-service

SpringBoot 3.5.12 · Java 21 · MongoDB 4.4 · Kafka 3.3.2 · WireMock 3.9.2

REST API for the Task Management app. See [API Endpoints](../README.md#-api-endpoints) for the full route list.

## Prerequisites

- JDK 21
- Maven 3.9+
- Docker (for integration tests — TestContainers spins up MongoDB and Kafka automatically)

## Running Tests

### Unit tests

```bash
mvn verify -DskipITs
```

### Unit + integration coverage (JaCoCo ≥90%)

Surefire and Failsafe append to the same `jacoco.exec`. The gate runs only when both suites run. Pact provider tests are excluded.

```bash
mvn verify -Dfailsafe.excludes='**/*PactProviderTest.java'
open target/site/jacoco/index.html
```

### Integration tests only

```bash
mvn verify -Pintegration-tests -Dfailsafe.excludes='**/*PactProviderTest.java'
```

### Mutation tests (PiTest ≥80%)

PIT runs the unit tests and the integration tests. Pact provider tests are excluded. Docker is required. `-Pmutation-tests` skips the Failsafe goal so the suite is not also run once outside PIT.

```bash
mvn verify -Pmutation-tests
open target/pit-reports/index.html
```

### Pact provider verification

Run as part of the full Pact pipeline — see [doc/pact.md](../doc/pact.md).

```bash
PACT_BROKER_BASE_URL=http://localhost:9292 \
  mvn verify -Pintegration-tests -Dit.test="*PactProviderTest" -Djacoco.skip=true
```
