package com.example.demo.e2e.test.edit

import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.TASK_MANAGEMENT
import com.example.demo.e2e.data.TaskResponse
import com.example.demo.e2e.test.base.MockedBackendTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import io.qameta.allure.kotlin.TmsLinks
import org.junit.Before
import org.junit.Test

@Epic(TASK_MANAGEMENT)
@Feature("Edit task")
@TmsLinks(TmsLink("101"), TmsLink("115"))
class EditTaskTest : MockedBackendTestBase() {
    private lateinit var context: TaskTestContext
    private lateinit var response: TaskResponse

    @Before
    fun setup() {
        context = TaskTestContext(
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
        )
        response = context.createTaskResponse()

        support().mock.api()
            .getTasks(response.toTask())
            .getTask(response.toTask())
            .getIsValid(true)
    }

    @Test
    fun shouldEditTask() {
        // Given
        val updatedContext = context.copy(
            title = "${context.title}-Updated",
            description = "${context.description}-Updated",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
        )
        val updatedResponse = updatedContext.createTaskResponse()

        steps.navigation.openMainPage()
        steps.navigation.refreshMainPage()

        // When
        val editTaskStep = steps.tasks
            .openTaskEditForm(response.title)
            .setTaskData(updatedContext.createTaskData())

        support().mock.api()
            .updateTask(updatedResponse.toTask())
            .getTasks(updatedResponse.toTask())
            .getTasks(updatedResponse.toTask())
            .getTask(updatedResponse.toTask())
            .getIsValid(true)

        editTaskStep.submitForm()

        steps.tasks.openTaskInfoForm(updatedContext.title)

        // Then
        validate.task.data(updatedContext.createTaskData())
    }
}
