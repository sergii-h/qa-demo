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

class MainPage(
    rule: AndroidComposeTestRule<*, MainActivity>,
) : ComposePage(rule) {
    fun createTaskButton() = node("add-task-button")

    fun pageTitle() = node("page-title")

    fun taskTitle(taskId: String) = node("task-title-${taskId}")

    fun taskTitleByTitle(title: String) = rule.onNode(
        hasText(title.trim()) and PageSemantics.hasTestTagPrefix("task-title-"),
    )

    fun infoButton(taskId: String) = node("info-button-${taskId}")

    fun editButton(taskId: String) = node("edit-button-${taskId}")

    fun deleteButton(taskId: String) = node("delete-button-${taskId}")

    fun statusTag(status: TaskStatus) = node("status-tag-${status.name}")

    fun priorityTag(priority: TaskPriority) = node("priority-tag-${priority.name}")

    fun waitUntilReady() {
        rule.waitUntil(timeoutMillis = 10_000) {
            rule.onAllNodesWithTag("loading-spinner").fetchSemanticsNodes().isEmpty() &&
                (
                    rule.onAllNodesWithTag("add-task-button").fetchSemanticsNodes().isNotEmpty() ||
                        rule.onAllNodesWithTag("empty-tasks").fetchSemanticsNodes().isNotEmpty() ||
                        rule.onAllNodesWithTag("load-error").fetchSemanticsNodes().isNotEmpty()
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
        waitUntilPresent("add-task-button")

    fun waitUntilLoadingSpinnerAbsent() = waitUntilAbsent("loading-spinner")

    fun pullToRefresh() {
        waitUntilPresent("task-list")
        node("task-list").performTouchInput {
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
                rule.onAllNodesWithTag("refreshing").fetchSemanticsNodes().isEmpty()
            }
        }
        rule.waitForIdle()
        waitUntilReady()
    }

    private fun waitUntilRefreshing(timeoutMillis: Long = 5_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodesWithTag("refreshing").fetchSemanticsNodes().isNotEmpty()
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
