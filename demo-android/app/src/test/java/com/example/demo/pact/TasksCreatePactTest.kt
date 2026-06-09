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
@PactTestFor(providerName = "demo-service-tasks-create")
class TasksCreatePactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun createTaskSuccessPact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("task title is unique")
            .uponReceiving("a valid task creation request")
            .path("/v1/tasks")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(PactFixtures.createTaskRequestBody())
            .willRespondWith()
            .status(201)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(PactFixtures.taskResponseBody())
    }

    @Test
    @PactTestFor(pactMethod = "createTaskSuccessPact")
    fun shouldHaveCreateTaskContractWhenPostingValidTask(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            api.createTask(PactFixtures.createTaskRequest())
        }
    }
}
