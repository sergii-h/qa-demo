package com.example.demo.e2e.interaction.page

import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.MainActivity

abstract class ComposePage(
    protected val rule: AndroidComposeTestRule<*, MainActivity>,
) {
    protected fun node(testTag: String) = rule.onNodeWithTag(testTag)

    protected fun waitUntilPresent(testTag: String, timeoutMillis: Long = 10_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
        }
    }

    protected fun waitUntilAbsent(testTag: String, timeoutMillis: Long = 10_000) {
        rule.waitUntil(timeoutMillis = timeoutMillis) {
            rule.onAllNodesWithTag(testTag).fetchSemanticsNodes().isEmpty()
        }
    }
}
