package com.example.demo.integration.support

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.AppLocaleTestSupport
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.runAsyncAction
import com.example.demo.ui.DemoNavHost
import com.example.demo.ui.TestTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IntegrationTestBase {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    protected lateinit var mockServer: IntegrationMockServer
    protected lateinit var repository: TaskRepository

    @Before
    fun setUpIntegrationHarness() {
        AppLocaleTestSupport.resetToEnglish()
        mockServer = IntegrationMockServer()
        mockServer.start()
        repository = TaskRepository(mockServer.createTaskApi())
    }

    @After
    fun tearDownIntegrationHarness() {
        mockServer.shutdown()
        AppLocaleTestSupport.resetToEnglish()
    }

    protected fun runAsyncAction(action: ComposeContentTestRule.() -> Unit) {
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher, action)
    }

    protected fun waitUntilCondition(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
        composeTestRule.waitUntil(timeoutMillis) {
            mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
            condition()
        }
    }

    protected fun launchApp() {
        runAsyncAction {
            setContent {
                DemoComposeTestTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }
        assertIsNotDisplayed(TestTags.LOADING_SPINNER)
    }

    protected fun switchLanguage(languageOption: LanguageOption) {
        runAsyncAction {
            onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
            onNodeWithTag(languageOption.testTag).performClick()
        }
        assertIsNotDisplayed(TestTags.LOADING_SPINNER)
    }

    fun assertTextEquals(testTag: String, expectedText: String) {
        waitUntilElementPresent(testTag)
        try {
            performTextEqualsAssertion(testTag, expectedText, useUnmergedTree = false)
        } catch (mergedTreeFailure: AssertionError) {
            try {
                performTextEqualsAssertion(testTag, expectedText, useUnmergedTree = true)
            } catch (_: AssertionError) {
                throw mergedTreeFailure
            }
        }
    }

    fun assertIsDisplayed(testTag: String) {
        waitUntilCondition {
            isNodeDisplayed(testTag, useUnmergedTree = false) ||
                isNodeDisplayed(testTag, useUnmergedTree = true)
        }
    }

    fun assertIsNotDisplayed(testTag: String) {
        waitUntilElementAbsent(testTag)
        try {
            performIsNotDisplayedAssertion(testTag, useUnmergedTree = false)
        } catch (mergedTreeFailure: AssertionError) {
            try {
                performIsNotDisplayedAssertion(testTag, useUnmergedTree = true)
            } catch (_: AssertionError) {
                throw mergedTreeFailure
            }
        }
    }

    private fun waitUntilElementPresent(testTag: String, timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            hasElement(testTag, useUnmergedTree = false) || hasElement(testTag, useUnmergedTree = true)
        }
    }

    private fun waitUntilElementAbsent(testTag: String, timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            !hasElement(testTag, useUnmergedTree = false) && !hasElement(testTag, useUnmergedTree = true)
        }
    }

    private fun hasElement(testTag: String, useUnmergedTree: Boolean): Boolean =
        composeTestRule.onAllNodesWithTag(testTag, useUnmergedTree)
            .fetchSemanticsNodes()
            .isNotEmpty()

    private fun performTextEqualsAssertion(
        testTag: String,
        expectedText: String,
        useUnmergedTree: Boolean,
    ) {
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
            .assertTextEquals(expectedText)
    }

    private fun performIsDisplayedAssertion(testTag: String, useUnmergedTree: Boolean) {
        val node = composeTestRule.onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
        runCatching { node.performScrollTo() }
        node.assertIsDisplayed()
    }

    private fun performIsNotDisplayedAssertion(testTag: String, useUnmergedTree: Boolean) {
        val node = composeTestRule.onNodeWithTag(testTag, useUnmergedTree = useUnmergedTree)
        if (hasElement(testTag, useUnmergedTree)) {
            runCatching { node.performScrollTo() }
            node.assertIsNotDisplayed()
        } else {
            node.assertDoesNotExist()
        }
    }

    private fun isNodeDisplayed(testTag: String, useUnmergedTree: Boolean): Boolean =
        runCatching { performIsDisplayedAssertion(testTag, useUnmergedTree) }.isSuccess
}
