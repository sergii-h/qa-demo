package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.data.TaskData
import com.example.demo.e2e.interaction.page.EditTaskForm
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure

class EditTaskStep(
    private val rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val editTaskForm = EditTaskForm(rule)
    private val mainPage = MainPage(rule)

    fun setTaskData(taskData: TaskData): EditTaskStep {
        Allure.step("Set task data") {
            editTaskForm.titleField().performTextClearance()
            editTaskForm.titleField().performTextInput(taskData.title)
            editTaskForm.descriptionField().performTextClearance()
            editTaskForm.descriptionField().performTextInput(taskData.description)
            editTaskForm.statusDropdown().performScrollTo().performClick()
            editTaskForm.statusOption(taskData.status).performClick()
            editTaskForm.priorityDropdown().performScrollTo().performClick()
            editTaskForm.priorityOption(taskData.priority).performClick()
        }
        return this
    }

    fun submitForm(): TaskTableStep {
        Allure.step("Submit 'Edit task' form") {
            editTaskForm.saveButton().performScrollTo().performClick()
            mainPage.waitUntilCreateTaskButtonPresent()
        }
        return TaskTableStep(rule)
    }
}
