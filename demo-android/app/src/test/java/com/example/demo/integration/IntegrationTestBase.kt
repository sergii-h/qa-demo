package com.example.demo.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.testing.waitUntilTagExists
import com.example.demo.ui.DemoNavHost
import com.example.demo.ui.TestTags
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.example.demo.testing.AppLocaleTestSupport
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
abstract class IntegrationTestBase {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    protected lateinit var fakeApi: FakeTaskApi
    protected lateinit var repository: TaskRepository

    @Before
    fun setUpIntegrationHarness() {
        AppLocaleTestSupport.resetToEnglish()
        fakeApi = FakeTaskApi()
        repository = TaskRepository(fakeApi)
    }

    @After
    fun tearDownIntegrationHarness() {
        AppLocaleTestSupport.resetToEnglish()
    }

    protected fun launchApp() {
        composeTestRule.setContent {
            DemoComposeTestTheme {
                DemoNavHost(repository = repository)
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun enqueueTasks(vararg tasks: Task) {
        fakeApi.enqueueGetTasks(tasks.toList())
    }

    /**
     * Enqueues two identical list responses. Required when a test switches language:
     * [LocalizedContent] rebuilds the tree and [TaskListViewModel] loads tasks again.
     */
    protected fun enqueueTasksForLanguageSwitch(vararg tasks: Task) {
        val list = tasks.toList()
        fakeApi.enqueueGetTasks(list)
        fakeApi.enqueueGetTasks(list)
    }

    protected fun enqueueTasksAfterLanguageSwitch(vararg tasks: Task) {
        fakeApi.enqueueGetTasks(tasks.toList())
    }

    protected fun enqueueGetTask(task: Task) {
        fakeApi.enqueueGetTask(task)
    }

    protected fun enqueueCreateTask(task: Task) {
        fakeApi.enqueueCreateTask(task)
    }

    protected fun enqueueUpdateTask(task: Task) {
        fakeApi.enqueueUpdateTask(task)
    }

    protected fun enqueueValidation(isValid: Boolean) {
        fakeApi.enqueueIsValid(isValid)
    }

    protected fun enqueueGetTasksError(code: Int, message: String? = null) {
        fakeApi.enqueueGetTasksError(code, message)
    }

    protected fun enqueueGetTaskError(code: Int, message: String? = null) {
        fakeApi.enqueueGetTaskError(code, message)
    }

    protected fun enqueueCreateTaskError(code: Int, message: String? = null) {
        fakeApi.enqueueCreateTaskError(code, message)
    }

    protected fun enqueueUpdateTaskError(code: Int, message: String? = null) {
        fakeApi.enqueueUpdateTaskError(code, message)
    }

    protected fun enqueueValidationError(code: Int, message: String? = null) {
        fakeApi.enqueueIsValidError(code, message)
    }

    protected fun enqueueDeleteSuccess() {
        fakeApi.enqueueDeleteSuccess()
    }

    protected fun enqueueDeleteError(code: Int, message: String? = null) {
        fakeApi.enqueueDeleteError(code, message)
    }

    protected fun enqueueGetTasksNetworkFailure() {
        fakeApi.enqueueGetTasksNetworkFailure()
    }

    protected fun enqueueGetTaskNetworkFailure() {
        fakeApi.enqueueGetTaskNetworkFailure()
    }

    protected fun enqueueValidationNetworkFailure() {
        fakeApi.enqueueIsValidNetworkFailure()
    }

    protected fun enqueueCreateTaskNetworkFailure() {
        fakeApi.enqueueCreateTaskNetworkFailure()
    }

    protected fun enqueueUpdateTaskNetworkFailure() {
        fakeApi.enqueueUpdateTaskNetworkFailure()
    }

    protected fun enqueueDeleteNetworkFailure() {
        fakeApi.enqueueDeleteNetworkFailure()
    }

    protected fun switchToSpanish() {
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_ES).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun switchToEnglish() {
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_OPTION_EN).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun assertTitleError(expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.TITLE_ERROR, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }

    protected fun assertLoadErrorDisplayed() {
        composeTestRule.waitUntilTagExists(TestTags.LOAD_ERROR)
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertIsDisplayed()
    }

    protected fun assertDescriptionText(expectedText: String) {
        composeTestRule.waitUntilTagExists(TestTags.DESCRIPTION)
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertTextEquals(expectedText)
    }

    protected fun assertFieldLabel(testTag: String, expectedText: String) {
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(testTag, useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        }
        composeTestRule.onNodeWithTag(testTag, useUnmergedTree = true)
            .assertTextEquals(expectedText)
    }

    protected fun selectPriority(priority: TaskPriority) {
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).performScrollTo().performClick()
        composeTestRule.waitUntilTagExists(TestTags.priorityDropdownOption(priority))
        composeTestRule.onNodeWithTag(TestTags.priorityDropdownOption(priority)).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun selectStatus(status: TaskStatus) {
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).performScrollTo().performClick()
        composeTestRule.waitUntilTagExists(TestTags.statusDropdownOption(status))
        composeTestRule.onNodeWithTag(TestTags.statusDropdownOption(status)).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }
}
