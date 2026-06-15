package com.example.demo.e2e.test.create

import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.TASK_MANAGEMENT
import com.example.demo.e2e.test.base.UatTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Test

@Epic(TASK_MANAGEMENT)
@Feature("Create task")
@TmsLink("100")
class CreateTaskUatTest : UatTestBase() {

    @Test
    fun shouldCreateTask() {
        // Given
        val context = TaskTestContext()
        steps.navigation.openMainPage()

        // When
        steps.tasks
            .openCreateTaskForm()
            .setTaskData(context.createTaskData())
            .submitForm()

        // Then
        validate.tasks.hasTask(context.title)

        // When
        steps.tasks.openTaskInfoForm(context.title)

        // Then
        validate.task.data(context.createTaskData())
    }
}
