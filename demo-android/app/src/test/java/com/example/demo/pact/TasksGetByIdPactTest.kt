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
@PactTestFor(providerName = "demo-service-tasks-get-by-id")
class TasksGetByIdPactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun getTaskByIdPact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("a task exists")
            .uponReceiving("a request for a task by id")
            .pathFromProviderState("/v1/tasks/\${taskId}", "/v1/tasks/${PactFixtures.TASK_ID}")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(PactFixtures.taskResponseBody())
    }

    @Test
    @PactTestFor(pactMethod = "getTaskByIdPact")
    fun shouldHaveGetTaskByIdContractWhenRequestingTaskDetails(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            api.getTask(PactFixtures.TASK_ID)
        }
    }
}
