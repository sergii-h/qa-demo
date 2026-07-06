package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

class CreateTaskForm(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun createButton() = node("create-button")

    fun titleField() = node("create-task-title-input")

    fun descriptionField() = node("task-description-input")

    fun statusDropdown() = node("status-dropdown")

    fun priorityDropdown() = node("priority-dropdown")

    fun statusOption(status: TaskStatus) = node("status-dropdown-option-${status.name}")

    fun priorityOption(priority: TaskPriority) = node("priority-dropdown-option-${priority.name}")
}
