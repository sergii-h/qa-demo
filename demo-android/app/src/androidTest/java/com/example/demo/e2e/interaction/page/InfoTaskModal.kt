package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

class InfoTaskModal(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun title() = node("modal-title")

    fun validIcon() = node("valid")

    fun descriptionField() = node("description")

    fun statusTag(status: TaskStatus) = node("status-tag-${status.name}")

    fun priorityTag(priority: TaskPriority) = node("priority-tag-${priority.name}")

    fun waitUntilVisible(timeoutMillis: Long = 15_000) {
        waitUntilPresent("modal-title", timeoutMillis)
        waitUntilAbsent("loading-spinner", timeoutMillis)
    }
}
