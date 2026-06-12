package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.data.TaskData
import com.example.demo.e2e.interaction.page.CreateTaskForm
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure

class CreateTaskStep(
    rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val createTaskForm = CreateTaskForm(rule)
    private val mainPage = MainPage(rule)

    fun setTaskData(taskData: TaskData): CreateTaskStep {
        Allure.step("Set task data") {
            createTaskForm.titleField().performTextInput(taskData.title)
            createTaskForm.descriptionField().performTextInput(taskData.description)
            createTaskForm.statusDropdown().performScrollTo().performClick()
            createTaskForm.statusOption(taskData.status).performClick()
            createTaskForm.priorityDropdown().performScrollTo().performClick()
            createTaskForm.priorityOption(taskData.priority).performClick()
        }
        return this
    }

    fun submitForm() {
        Allure.step("Submit 'Create task' form") {
            createTaskForm.createButton().performScrollTo().performClick()
            mainPage.waitUntilCreateTaskButtonPresent()
        }
    }
}
