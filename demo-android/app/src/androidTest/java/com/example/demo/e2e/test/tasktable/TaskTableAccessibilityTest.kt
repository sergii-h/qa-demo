package com.example.demo.e2e.test.tasktable

import com.example.demo.e2e.context.TaskTestContext
import com.example.demo.e2e.data.AllureEpic.ACCESSIBILITY
import com.example.demo.e2e.test.base.AccessibilityTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Before
import org.junit.Test

@Epic(ACCESSIBILITY)
@Feature("Task table")
@TmsLink("98")
class TaskTableAccessibilityTest : AccessibilityTestBase() {
    @Before
    fun setup() {
        support().mock.api().getTasks(
            TaskTestContext().createTaskResponse().toTask(),
            TaskTestContext().createTaskResponse().toTask())
    }

    @Test
    fun shouldHaveNoAccessibilityViolationsOnTaskTableWhenTasksLoaded() {
        // Given
        steps.navigation.openMainPage()
        steps.navigation.refreshMainPage()

        // When
        steps.accessibility.analyze()

        // Then
        validate.accessibility.hasNoViolations()
    }
}
