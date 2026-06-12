package com.example.demo.e2e.interaction.validation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.e2e.data.TaskData
import com.example.demo.e2e.interaction.page.InfoTaskModal
import io.qameta.allure.kotlin.Allure

class TaskValidator(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val infoTaskModal = InfoTaskModal(rule)

    fun data(taskData: TaskData): TaskValidator {
        Allure.step("Validate task info data") {
            infoTaskModal.title().assertTextEquals(taskData.title)
            infoTaskModal.descriptionField().assertTextEquals(taskData.description)
            infoTaskModal.statusTag(taskData.status).assertIsDisplayed()
            infoTaskModal.priorityTag(taskData.priority).assertIsDisplayed()
        }
        return this
    }

    fun isValid() {
        Allure.step("Validate task is marked as valid") {
            infoTaskModal.validIcon().assertIsDisplayed()
        }
    }
}
