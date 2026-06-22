package com.example.demo.integration.support

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.ui.TestTags

class CommonAssertions(
    private val composeTestRule: ComposeContentTestRule,
    private val advanceCoroutines: () -> Unit = {},
) {

    fun assertTitleError(expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            advanceCoroutines()
            composeTestRule.onAllNodesWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }

    fun assertSaveError(expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            advanceCoroutines()
            composeTestRule.onAllNodesWithTag(TestTags.SAVE_ERROR)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.SAVE_ERROR).assertTextEquals(expectedText)
    }

    fun assertLoadErrorDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            advanceCoroutines()
            composeTestRule.onAllNodesWithTag(TestTags.LOAD_ERROR)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertIsDisplayed()
    }

    fun assertErrorSnackbarDisplayed() {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            advanceCoroutines()
            runCatching {
                composeTestRule.onNodeWithTag(TestTags.ERROR_SNACKBAR, useUnmergedTree = true)
                    .assertIsDisplayed()
            }.isSuccess
        }
        composeTestRule.onNodeWithTag(TestTags.ERROR_SNACKBAR, useUnmergedTree = true)
            .assertIsDisplayed()
    }

    fun assertDescriptionText(expectedText: String) {
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertTextEquals(expectedText)
    }

    fun assertFieldLabel(testTag: String, expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            advanceCoroutines()
            composeTestRule.onAllNodesWithTag(testTag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }
}
