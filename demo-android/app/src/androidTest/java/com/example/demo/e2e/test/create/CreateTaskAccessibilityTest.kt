package com.example.demo.e2e.test.create

import com.example.demo.e2e.data.AllureEpic.ACCESSIBILITY
import com.example.demo.e2e.interaction.page.MainPage
import com.example.demo.e2e.test.base.AccessibilityTestBase
import io.qameta.allure.kotlin.Epic
import io.qameta.allure.kotlin.Feature
import io.qameta.allure.kotlin.TmsLink
import org.junit.Before
import org.junit.Test

@Epic(ACCESSIBILITY)
@Feature("Create task")
@TmsLink("100")
class CreateTaskAccessibilityTest : AccessibilityTestBase() {

    @Before
    fun setup() {
        support().mock.api().getTasks()
    }

    @Test
    fun shouldHaveNoAccessibilityViolationsOnCreateTaskForm() {
        // Given
        steps.navigation.openMainPage()
        steps.tasks.openCreateTaskForm()

        // When
        steps.accessibility.analyze()

        // Then
        validate.accessibility.hasNoViolations()
    }
}
