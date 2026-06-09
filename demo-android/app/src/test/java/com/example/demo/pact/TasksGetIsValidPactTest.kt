package com.example.demo.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "demo-service-tasks-get-is-valid")
class TasksGetIsValidPactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun getIsValidPact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("validation result is true for the task")
            .uponReceiving("a request for task validation status")
            .pathFromProviderState(
                "/v1/tasks/isValid/\${taskId}",
                "/v1/tasks/isValid/${PactFixtures.TASK_ID}"
            )
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body("true")
    }

    @Test
    @PactTestFor(pactMethod = "getIsValidPact")
    fun shouldHaveGetIsValidContractWhenRequestingValidationStatus(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            api.isValid(PactFixtures.TASK_ID)
        }
    }
}
