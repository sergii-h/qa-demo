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
@PactTestFor(providerName = "demo-service-tasks-update")
class TasksUpdateDuplicatePactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun updateTaskDuplicatePact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("another task has the requested title")
            .uponReceiving("a task update request with duplicate title")
            .pathFromProviderState("/v1/tasks/\${taskId}", "/v1/tasks/${PactFixtures.TASK_ID}")
            .method("PUT")
            .headers("Content-Type", "application/json")
            .body(PactFixtures.updateTaskRequestBody())
            .willRespondWith()
            .status(409)
            .headers(mapOf("Content-Type" to "application/json"))
            .body(
                PactFixtures.duplicateTitleErrorBody(
                    "Task with title '${PactFixtures.updateTaskRequest().title}' already exists"
                )
            )
    }

    @Test
    @PactTestFor(pactMethod = "updateTaskDuplicatePact")
    fun shouldHaveUpdateTaskDuplicateContractWhenUpdatingWithDuplicateTitle(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            try {
                api.updateTask(PactFixtures.TASK_ID, PactFixtures.updateTaskRequest())
                error("Expected HttpException")
            } catch (e: HttpException) {
                // Then
                assertThat(e.code()).isEqualTo(409)
            }
        }
    }
}
