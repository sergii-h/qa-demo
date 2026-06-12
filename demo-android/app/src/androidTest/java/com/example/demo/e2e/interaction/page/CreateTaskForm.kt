package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.ui.TestTags

class CreateTaskForm(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun createButton() = node(TestTags.CREATE_BUTTON)

    fun titleField() = node(TestTags.CREATE_TASK_TITLE_INPUT)

    fun descriptionField() = node(TestTags.TASK_DESCRIPTION_INPUT)

    fun statusDropdown() = node(TestTags.STATUS_DROPDOWN)

    fun priorityDropdown() = node(TestTags.PRIORITY_DROPDOWN)

    fun statusOption(status: TaskStatus) = node(TestTags.statusDropdownOption(status))

    fun priorityOption(priority: TaskPriority) = node(TestTags.priorityDropdownOption(priority))
}
