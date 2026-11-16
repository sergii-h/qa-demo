# Migration from Testing Pyramid to Risk-Based Test Layering

Evidence for the decision recorded in [ADR 007](adr/007-unit-vs-integration-test-boundary-checklist.md): what the tests looked like under a testing pyramid, what they look like now, and what changed in the numbers.

The strategy itself is explained in the ADR and in the [README](../README.md#-testing-strategy). This document only records the before and after.

## Baselines

The pre-migration state of each platform is kept as a git tag, so it can be checked out, built and compared at any time.

| Platform | Strategy before | Baseline tag | Status |
|---|---|---|---|
| Backend (`demo-service`) | Testing pyramid, mock-heavy unit tests | `pyramid-baseline-be` (commit `ebd3e107`) | Migrated |
| Frontend (`demo-interface`) | Testing pyramid | `pyramid-baseline-fe` (to be created) | Planned |

```bash
git checkout pyramid-baseline-be
cd demo-service && mvn verify
```

A new platform is added to this document by creating its `pyramid-baseline-<platform>` tag on the last commit before the migration, then adding a section below.

## Backend

Measured on 2026-11-15, same machine, both runs sequential.

| State | Revision |
|---|---|
| Before | tag `pyramid-baseline-be` |
| After | branch `qa-demo-150-refactor-be-tests` at `00053456` plus uncommitted `demo-service` changes (replace with the final commit or tag once merged) |

### Results

Pact provider tests are left out of every row: the same seven fail on both sides in the local run because the local Pact Broker rejects the request (HTTP 400, `providerVersionBranch must be specified`). That is an environment issue, not a result of the migration.

| Metric | Before | After |
|---|---|---|
| Unit tests (surefire) | 39 | 9 |
| Integration tests (failsafe, without Pact) | 23 | 27 |
| Unit test classes (top level) | 8 | 3 |
| Test files importing Mockito | 8 | 2 |
| `@Mock` fields | 13 | 4 |
| `verify(` calls | 55 | 13 |
| `when(` stubs | 47 | 11 |
| Sum of test case times, unit | 16.0 s | 7.4 s |
| Sum of test case times, integration (incl. Pact errors) | 38.1 s | 27.0 s |
| JaCoCo line coverage | 100% (138 lines) | 100% (121 lines) |
| JaCoCo branch coverage | 100% (10 branches) | 100% (10 branches) |
| PIT mutations killed | 31 / 31 | 28 / 28 |
| PIT surviving or uncovered mutants | 0 | 0 |
| PIT tests that ran | unit tests only | unit and integration tests |
| PIT duration | 11 s | 107 s |

The sums of test case times are larger than wall-clock time because classes run in parallel.

### Read the numbers with these caveats

- **Coverage and mutation score did not drop, but they are not strictly comparable.** The after state excludes `DemoServiceApplication`, `CorsConfigurer` and `config/**` from JaCoCo and PIT, so it measures 121 lines against 138, and PIT generates 28 mutations instead of 31.
- **PIT now runs the integration tests.** Before, PIT only saw the unit tests (`integration.*` was excluded). After, it runs unit and integration tests together, which is what makes it a valid check that deleted unit tests are still covered. The cost is a run that takes about ten times longer (11 s to 107 s).
- **The branch changes more than the tests.** It also upgrades Spring Boot 3.5 to 4.1 and REST Assured 5 to 6, and adds a `Clock` bean so timestamps can be asserted. Runtimes are therefore indicative, not a controlled benchmark.
- **A timeout is counted as a kill by PIT.** One mutant timed out before and four after (all in `TaskEventProducer` and `TaskController`). They are included in the killed totals above.

### What was removed and where it is covered now

| Removed unit tests | Now covered by |
|---|---|
| `TaskExternalClientTest` (3 tests, whole class) | `ValidateItemTest` with WireMock |
| `DemoExceptionHandlerTest`: not found, duplicate title, two validation tests (4 of 5) | 404 and 409 assertions in the API integration tests, and the parameterised 400 test in `CreateTaskTest` |
| `TaskControllerTest`: 14 of 21 cases (happy paths, argument-capture checks, get and list, validity) | `CreateTaskTest`, `UpdateTaskTest`, `RetrieveTaskTest`, `ListTasks*Test`, `DeleteTaskTest`, `ValidateItemTest` |
| `TaskEventProducerTest`: the 3 produce-event cases (3 of 4) | `TaskCreatedEventTest`, `TaskUpdatedEventTest`, `TaskDeletedEventTest` against a real Kafka container |
| `CorsConfigurerTest`, `DemoServiceApplicationTest`, `KafkaConfigTest`, `WebClientConfigTest` (6 tests) | Nothing directly: these classes are excluded from JaCoCo and PIT, and their wiring is exercised by every integration test |

Remaining unit tests (9):

- `TaskControllerTest` (7): persistence throwing so that no event is published (create, update, delete), plus the not-found and duplicate-title rejections.
- `TaskEventProducerTest` (1): the Kafka send failure is logged.
- `DemoExceptionHandlerTest` (1): `DuplicateKeyException` mapped to 409.

The not-found and duplicate-title unit cases overlap with the 404 and 409 integration assertions. They are candidates for removal in a follow-up, subject to the PIT check.

### Example 1: mocked client versus a real HTTP call

Before, `TaskExternalClientTest` stubbed the whole fluent `WebClient` chain with five mocks and then asserted that the mock returned what it was told to:

```java
@Mock private WebClient webClient;
@Mock private WebClient.RequestBodyUriSpec requestBodyUriSpec;
@Mock private WebClient.RequestBodySpec requestBodySpec;
@Mock private WebClient.RequestHeadersSpec requestHeadersSpec;
@Mock private WebClient.ResponseSpec responseSpec;

when(webClient.post()).thenReturn(requestBodyUriSpec);
when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);
when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);
when(requestBodySpec.bodyValue(any())).thenReturn(requestHeadersSpec);
when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
when(responseSpec.bodyToMono(Boolean.class)).thenReturn(Mono.just(true));
```

That test stays green whatever the real request looks like, and it breaks if the client is rewritten in a different style. After, `ValidateItemTest` goes through the public endpoint and checks the request that actually reaches the third party:

```java
WireMock.verify(postRequestedFor(urlEqualTo("/external/validate/task"))
        .withHeader("Content-Type", equalTo("application/json"))
        .withRequestBody(equalToJson("""
                {
                  "title": "Test Title",
                  "description": "Test Description",
                  "status": "IN_PROGRESS",
                  "priority": "MEDIUM"
                }
                """))
);
```

It also covers a third-party failure (`shouldReturnHttp500WhenExternalValidationServiceFails`) and checks that no outbound call is made for an unknown task.

### Example 2: error handling through the HTTP boundary

Before, `DemoExceptionHandlerTest` mocked `MethodArgumentNotValidException` and `BindingResult` to check the handler's output in isolation:

```java
MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
BindingResult bindingResult = mock(BindingResult.class);
when(exception.getBindingResult()).thenReturn(bindingResult);
when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError1, fieldError2));
```

This proves the handler maps whatever it is given, not that an invalid request produces that response. After, `CreateTaskTest` sends real invalid bodies and asserts the status and the exact error body for each rule:

```java
@ParameterizedTest(name = "{1}")
@MethodSource("validationErrorScenarios")
void shouldReturnHttp400ForValidationErrors(String field, String message, String requestBody) {
    Response response = requestSpec.body(requestBody).post("/tasks");

    assertAll(
            () -> assertThat(response.statusCode(), is(400)),
            () -> assertThat(response.jsonPath().<String, String>getMap("$"), is(Map.of(field, message)))
    );
}
```

The integration test used to check only the status code. It now checks the response body too, and the description length limit, which had no backend test before, is covered.

### Lessons

- Deleting a mocked test was safe only where the scenario was already provable at the boundary. In several places the integration test had to be strengthened first (response body, outbound request body, timestamps).
- Mutation testing is what confirms a deletion. It only works as a check when PIT runs the integration tests, which is why that configuration changed.
- The suite stays cheap: `mvn verify` took about 27 s after (36 s before) on a laptop, with Pact provider tests failing early in both runs, so a full run with a working broker will be somewhat longer.

## Frontend

Not migrated yet. When it is, create `pyramid-baseline-fe` on the last commit before the change, collect the same metrics (Vitest unit and integration test counts, number of mocked modules, coverage, Stryker score and duration), and add a section here with the same structure as the backend one.
