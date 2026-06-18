package com.example.demo.e2e.test.create

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
@Feature("Create task")
@TmsLink("100")
class CreateTaskTest : MockedBackendTestBase() {
    private lateinit var context: TaskTestContext
    private lateinit var response: TaskResponse

    @Before
    fun setup() {
        context = TaskTestContext()
        response = context.createTaskResponse()

        support().mock.api()
            .createTask(response.toTask())
            .getTasks(response.toTask())
            .getTasks(response.toTask())
            .getTask(response.toTask())
            .getIsValid(true)
    }

    @Test
    fun shouldCreateTask() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.tasks
            .openCreateTaskForm()
            .setTaskData(context.createTaskData())
            .submitForm()

        // Then
        validate.tasks.hasTask(response.title)

        // When
        steps.tasks.openTaskInfoForm(response.title)

        // Then
        validate.task.data(context.createTaskData())
    }
}
