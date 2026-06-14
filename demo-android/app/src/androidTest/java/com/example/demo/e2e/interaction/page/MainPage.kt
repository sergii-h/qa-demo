package com.example.demo.e2e.interaction.page

import androidx.compose.ui.semantics.SemanticsNode
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.example.demo.MainActivity
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.ui.TestTags

class MainPage(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun createTaskButton() = node(TestTags.ADD_TASK_BUTTON)

    fun pageTitle() = node(TestTags.PAGE_TITLE)

    fun taskTitle(taskId: String) = node(TestTags.taskTitle(taskId))

    fun taskTitleByTitle(title: String) = rule.onNode(
        hasText(title.trim()) and PageSemantics.hasTestTagPrefix("task-title-"),
    )

    fun infoButton(taskId: String) = node(TestTags.infoButton(taskId))

    fun editButton(taskId: String) = node(TestTags.editButton(taskId))

    fun deleteButton(taskId: String) = node(TestTags.deleteButton(taskId))

    fun statusTag(status: TaskStatus) = node(TestTags.statusTag(status))

    fun priorityTag(priority: TaskPriority) = node(TestTags.priorityTag(priority))

    fun waitUntilReady() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithTag(TestTags.LOADING_SPINNER).fetchSemanticsNodes().isEmpty() &&
                (
                    rule.onAllNodesWithTag(TestTags.ADD_TASK_BUTTON).fetchSemanticsNodes().isNotEmpty() ||
                        rule.onAllNodesWithTag(TestTags.EMPTY_TASKS).fetchSemanticsNodes().isNotEmpty() ||
                        rule.onAllNodesWithTag(TestTags.LOAD_ERROR).fetchSemanticsNodes().isNotEmpty()
                    )
        }
    }

    fun waitUntilTaskWithTitlePresent(title: String, timeoutMillis: Long = 10_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodes(PageSemantics.hasTestTagPrefix("task-title-"))
                .fetchSemanticsNodes()
                .any { node -> PageSemantics.nodeText(node) == title.trim() }
        }
    }

    fun waitUntilTaskWithTitleAbsent(title: String, timeoutMillis: Long = 10_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodes(PageSemantics.hasTestTagPrefix("task-title-"))
                .fetchSemanticsNodes()
                .none { node -> PageSemantics.nodeText(node) == title.trim() }
        }
    }

    fun waitUntilCreateTaskButtonPresent() =
        waitUntilPresent(TestTags.ADD_TASK_BUTTON)

    fun waitUntilLoadingSpinnerAbsent() = waitUntilAbsent(TestTags.LOADING_SPINNER)

    fun pullToRefresh() {
        waitUntilPresent(TestTags.TASK_LIST)
        node(TestTags.TASK_LIST).performTouchInput {
            swipeDown(
                startY = top + height * 0.05f,
                endY = top + height * 0.95f,
                durationMillis = 400,
            )
        }
    }

    fun waitUntilRefreshComplete(timeoutMillis: Long = 15_000) {
        val refreshStarted = runCatching {
            waitUntilRefreshing(timeoutMillis = 2_000)
        }.isSuccess
        if (refreshStarted) {
            rule.waitUntil(timeoutMillis = timeoutMillis) {
                rule.onAllNodesWithTag(TestTags.REFRESHING).fetchSemanticsNodes().isEmpty()
            }
        }
        rule.waitForIdle()
        waitUntilReady()
    }

    private fun waitUntilRefreshing(timeoutMillis: Long = 5_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodesWithTag(TestTags.REFRESHING).fetchSemanticsNodes().isNotEmpty()
        }
    }
}

internal object PageSemantics {
    fun hasTestTagPrefix(prefix: String): SemanticsMatcher = SemanticsMatcher(
        "TestTag starts with '$prefix'",
    ) { node ->
        testTag(node)?.startsWith(prefix) == true
    }

    fun testTag(node: SemanticsNode): String? =
        node.config.let { config ->
            if (config.contains(SemanticsProperties.TestTag)) {
                config[SemanticsProperties.TestTag]
            } else {
                null
            }
        }

    fun nodeText(node: SemanticsNode): String? =
        node.config.let { config ->
            if (config.contains(SemanticsProperties.Text)) {
                config[SemanticsProperties.Text].firstOrNull()?.text
            } else {
                null
            }
        }
}
