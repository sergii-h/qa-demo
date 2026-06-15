package com.example.demo.e2e.interaction.validation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure

class TasksValidator(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val mainPage = MainPage(rule)

    fun hasTask(title: String) {
        Allure.step("Validate task list has task '$title'") {
            val normalizedTitle = title.trim()
            mainPage.waitUntilTaskWithTitlePresent(normalizedTitle)
            mainPage.taskTitleByTitle(normalizedTitle).assertIsDisplayed()
        }
    }

    fun hasNoTask(title: String) {
        Allure.step("Validate task '$title' is removed from list") {
            mainPage.waitUntilTaskWithTitleAbsent(title.trim())
        }
    }
}
