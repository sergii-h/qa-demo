package com.example.demo.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactBuilder
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.V4Pact
import au.com.dius.pact.core.model.annotations.Pact
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import retrofit2.HttpException

@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "demo-service-tasks-create")
class TasksCreateDuplicatePactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun createTaskDuplicatePact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("task title already exists")
            .uponReceiving("a task creation request with duplicate title")
            .path("/v1/tasks")
            .method("POST")
            .headers("Content-Type", "application/json")
            .body(PactFixtures.createTaskRequestBody())
            .willRespondWith()
            .status(409)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(
                PactFixtures.duplicateTitleErrorBody(
                    "Task with title 'Prepare release notes' already exists"
                )
            )
    }

    @Test
    @PactTestFor(pactMethod = "createTaskDuplicatePact")
    fun shouldHaveCreateTaskDuplicateContractWhenPostingDuplicateTitle(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            try {
                api.createTask(PactFixtures.createTaskRequest())
                error("Expected HttpException")
            } catch (e: HttpException) {
                // Then
                assertThat(e.code()).isEqualTo(409)
            }
        }
    }
}
