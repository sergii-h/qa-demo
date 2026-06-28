package com.example.demo.testing

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import com.example.demo.locale.AppLocale
import com.example.demo.locale.LocalizedContent
import com.example.demo.ui.theme.DemoTheme
import kotlinx.coroutines.test.TestDispatcher

@Composable
fun DemoComposeTestTheme(content: @Composable () -> Unit) {
    val languageTag by AppLocale.languageTag.collectAsState()
    LocalizedContent(languageTag = languageTag) {
        DemoTheme(content = content)
    }
}

fun ComposeContentTestRule.advanceComposeCoroutineIdle(dispatcher: TestDispatcher) {
    waitForIdle()
    dispatcher.scheduler.advanceUntilIdle()
    waitForIdle()
}

inline fun ComposeContentTestRule.runAsyncAction(
    dispatcher: TestDispatcher,
    crossinline action: ComposeContentTestRule.() -> Unit,
) {
    action()
    advanceComposeCoroutineIdle(dispatcher)
}

fun ComposeContentTestRule.waitUntilTextExists(text: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
    }
}

fun ComposeContentTestRule.waitUntilTagExists(testTag: String, timeoutMillis: Long = 5_000) {
    waitUntil(timeoutMillis) {
        onAllNodesWithTag(testTag).fetchSemanticsNodes().isNotEmpty()
    }
}
