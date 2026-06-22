package com.example.demo.integration.support

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.AppLocaleTestSupport
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.advanceComposeCoroutineIdle
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

    protected fun flushAsyncWork() {
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun advanceCoroutineIdle() {
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    }

    protected fun waitUntilCondition(timeoutMillis: Long = 5_000, condition: () -> Boolean) {
        composeTestRule.waitUntil(timeoutMillis) {
            advanceCoroutineIdle()
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
        waitUntilListLoaded()
    }

    protected fun waitUntilListLoaded(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.LOADING_SPINNER, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    protected fun waitUntilCreateFormClosed(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.CREATE_TASK_TITLE_INPUT)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    protected fun waitUntilEditFormLoaded(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.EDIT_TASK_TITLE_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntilDetailLoaded(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.DESCRIPTION)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun openCreateForm() {
        runAsyncAction {
            onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()
        }
        waitUntilCondition {
            composeTestRule.onAllNodesWithTag(TestTags.CREATE_TASK_TITLE_INPUT)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun openEditForm(taskId: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.editButton(taskId)).performClick()
        }
        waitUntilEditFormLoaded()
    }

    protected fun openDetail(taskId: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.infoButton(taskId)).performClick()
        }
        waitUntilDetailLoaded()
    }

    protected fun openDetailExpectingLoadError(taskId: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.infoButton(taskId)).performClick()
        }
        waitUntilCondition {
            composeTestRule.onAllNodesWithTag(TestTags.LOAD_ERROR)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntilEditFormClosed(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.EDIT_TASK_TITLE_INPUT)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    protected fun waitUntilTaskTitleVisible(taskId: String, timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.taskTitle(taskId))
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
    }

    protected fun waitUntilRefreshFinished(timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            composeTestRule.onAllNodesWithTag(TestTags.REFRESHING, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isEmpty()
        }
    }

    protected val commonAssertions: CommonAssertions
        get() = CommonAssertions(composeTestRule, ::advanceCoroutineIdle)

    protected fun switchLanguage(languageOption: LanguageOption) {
        runAsyncAction {
            onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
            onNodeWithTag(languageOption.testTag).performClick()
        }
        waitUntilListLoaded()
    }
}