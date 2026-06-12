package com.example.demo.e2e.interaction.validation

import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure

class LanguageValidator(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val mainPage = MainPage(rule)

    fun uiIsInSpanish() {
        Allure.step("Validate table headers and buttons are in Spanish") {
            mainPage.pageTitle().assertTextEquals("Tareas")
            mainPage.createTaskButton().assertContentDescriptionEquals("Crear tarea")
        }
    }

    fun statusTagShowsText(status: TaskStatus, expectedText: String) {
        Allure.step("Validate status tag for '${status.name}' shows '$expectedText'") {
            mainPage.statusTag(status).assertTextEquals(expectedText)
        }
    }

    fun priorityTagShowsText(priority: TaskPriority, expectedText: String) {
        Allure.step("Validate priority tag for '${priority.name}' shows '$expectedText'") {
            mainPage.priorityTag(priority).assertTextEquals(expectedText)
        }
    }
}
