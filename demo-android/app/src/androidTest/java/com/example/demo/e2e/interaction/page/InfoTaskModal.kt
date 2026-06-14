package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.ui.TestTags

class InfoTaskModal(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun title() = node(TestTags.MODAL_TITLE)

    fun validIcon() = node(TestTags.VALID)

    fun descriptionField() = node(TestTags.DESCRIPTION)

    fun statusTag(status: TaskStatus) = node(TestTags.statusTag(status))

    fun priorityTag(priority: TaskPriority) = node(TestTags.priorityTag(priority))

    fun waitUntilVisible(timeoutMillis: Long = 15_000) {
        waitUntilPresent(TestTags.MODAL_TITLE, timeoutMillis)
        waitUntilAbsent(TestTags.LOADING_SPINNER, timeoutMillis)
    }
}
