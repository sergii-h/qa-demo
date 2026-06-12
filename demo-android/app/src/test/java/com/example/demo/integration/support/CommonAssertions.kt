package com.example.demo.integration.support

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.ui.TestTags

class CommonAssertions(
    private val composeTestRule: ComposeContentTestRule,
) {

    fun assertTitleError(expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }

    fun assertLoadErrorDisplayed() {
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertIsDisplayed()
    }

    fun assertDescriptionText(expectedText: String) {
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertTextEquals(expectedText)
    }

    fun assertFieldLabel(testTag: String, expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(testTag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }
}
