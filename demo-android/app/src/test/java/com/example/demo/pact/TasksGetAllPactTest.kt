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
@PactTestFor(providerName = "demo-service-tasks-get-all")
class TasksGetAllPactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun getAllTasksPact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("tasks exist")
            .uponReceiving("a request for all tasks")
            .path("/v1/tasks")
            .method("GET")
            .willRespondWith()
            .status(200)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(PactFixtures.taskListResponseBody())
    }

    @Test
    @PactTestFor(pactMethod = "getAllTasksPact")
    fun shouldHaveGetAllTasksContractWhenRequestingAllTasks(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            api.getTasks()
        }
    }
}
