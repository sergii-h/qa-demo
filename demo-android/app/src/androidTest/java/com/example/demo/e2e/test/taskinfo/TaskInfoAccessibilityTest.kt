package com.example.demo.e2e.test.taskinfo

import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.ACCESSIBILITY
import com.example.demo.e2e.data.TaskResponse
import com.example.demo.e2e.test.base.AccessibilityTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Before
import org.junit.Test

@Epic(ACCESSIBILITY)
@Feature("View task info")
@TmsLink("102")
class TaskInfoAccessibilityTest : AccessibilityTestBase() {
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
    fun shouldHaveNoAccessibilityViolationsOnTaskInfoForm() {
        // Given
        steps.navigation.openMainPage()
        steps.navigation.refreshMainPage()
        steps.tasks.openTaskInfoForm(response.title)

        // When
        steps.accessibility.analyze()

        // Then
        validate.accessibility.hasNoViolations()
    }
}
