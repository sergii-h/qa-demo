package com.example.demo.e2e.test.taskinfo

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
@Feature("View task info")
@TmsLinks(TmsLink("102"), TmsLink("115"))
class TaskInfoTest : MockedBackendTestBase() {
    private lateinit var context: TaskTestContext
    private lateinit var response: TaskResponse

    @Before
    fun setup() {
        context = TaskTestContext()
        response = context.createTaskResponse()

        support().mock.api().getTasks(response.toTask())
        support().mock.api().getTask(response.toTask())
        support().mock.api().getIsValid(true)
    }

    @Test
    fun shouldViewTaskInfo() {
        // Given
        steps.navigation.openMainPage()
        steps.navigation.refreshMainPage()

        // When
        steps.tasks.openTaskInfoForm(response.title)

        // Then
        validate.task
            .data(context.createTaskData())
            .isValid()
    }
}
