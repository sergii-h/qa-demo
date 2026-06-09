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
@PactTestFor(providerName = "demo-service-tasks-delete")
class TasksDeletePactTest {

    @Pact(consumer = PactFixtures.CONSUMER)
    fun deleteTaskPact(builder: PactBuilder): V4Pact = builder.legacyHttpPact {
        given("a task exists to delete")
            .uponReceiving("a request to delete a task")
            .pathFromProviderState("/v1/tasks/\${taskId}", "/v1/tasks/${PactFixtures.TASK_ID}")
            .method("DELETE")
            .willRespondWith()
            .status(204)
    }

    @Test
    @PactTestFor(pactMethod = "deleteTaskPact")
    fun shouldHaveDeleteTaskContractWhenDeletingTaskById(mockServer: MockServer) {
        runBlocking {
            // Given
            val api = PactFixtures.createTaskApi(mockServer)

            // When
            api.deleteTask(PactFixtures.TASK_ID)
        }
    }
}
