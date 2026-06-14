package com.example.demo.e2e.test.delete

import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.TASK_MANAGEMENT
import com.example.demo.e2e.data.TaskResponse
import com.example.demo.e2e.test.base.MockedBackendTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Before
import org.junit.Test

@Epic(TASK_MANAGEMENT)
@Feature("Delete task")
@TmsLink("99")
@TmsLink("115")
class DeleteTaskTest : MockedBackendTestBase() {
    private lateinit var context: TaskTestContext
    private lateinit var response: TaskResponse

    @Before
    fun setup() {
        context = TaskTestContext()
        response = context.createTaskResponse()

        support().mock.api().getTasks(response.toTask())
        support().mock.api().deleteTask()
    }

    @Test
    fun shouldDeleteTask() {
        // Given
        steps.navigation.openMainPage()
        steps.navigation.refreshMainPage()

        // When
        steps.tasks.deleteTask(response.title)

        // Then
        validate.tasks.hasNoTask(response.title)
    }
}
