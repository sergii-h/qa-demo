package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

class EditTaskForm(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun saveButton() = node("save-button")

    fun titleField() = node("edit-task-title-input")

    fun descriptionField() = node("task-description-input")

    fun statusDropdown() = node("status-dropdown")

    fun priorityDropdown() = node("priority-dropdown")

    fun statusOption(status: TaskStatus) = node("status-dropdown-option-${status.name}")

    fun priorityOption(priority: TaskPriority) = node("priority-dropdown-option-${priority.name}")
}
