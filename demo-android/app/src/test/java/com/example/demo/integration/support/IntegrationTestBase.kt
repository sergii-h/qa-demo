package com.example.demo.integration.support

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
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

    protected fun runAsyncAction(action: ComposeContentTestRule.() -> Unit) {
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher, action)
    }

    protected fun flushAsyncWork() {
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
    }

    protected fun launchApp() {
        runAsyncAction {
            setContent {
                DemoComposeTestTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }
    }

    protected val commonAssertions: CommonAssertions
        get() = CommonAssertions(composeTestRule)

    protected fun switchLanguage(languageOption: LanguageOption) {
        runAsyncAction {
            onNodeWithTag(TestTags.LANGUAGE_SWITCHER).performClick()
            onNodeWithTag(languageOption.testTag).performClick()
        }
    }
}