package com.example.demo.ui.tasklist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.example.demo.locale.AppLocale
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.RecordingLocalizedContent
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.assertHasTranslations
import com.example.demo.testing.runAsyncAction
import com.example.demo.ui.TestTags
import com.example.demo.ui.theme.DemoTheme
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(ParameterizedRobolectricTestRunner::class)
class TaskListScreenTranslationTest(
    private val languageTag: String,
) {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository = mockk<TaskRepository>()
    private val recorded = mutableListOf<Int>()
    private val context = RuntimeEnvironment.getApplication()

    @Before
    fun setUp() {
        recorded.clear()
        AppLocale.setLanguage(context, languageTag)
        coEvery { repository.getTasks() } returns listOf(
            TaskFixtures.sampleTask.copy(
                id = "task-translation",
                title = "Task title",
                description = "Task description",
            ),
        )
    }

    @After
    fun tearDown() {
        AppLocale.setLanguage(context, AppLocale.ENGLISH)
    }

    @Test
    fun shouldHaveTranslationsForTasksTable() {
        // Given
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                RecordingLocalizedContent(languageTag, recorded) {
                    DemoTheme {
                        TaskListScreen(
                            repository = repository,
                            onCreateTask = {},
                            onEditTask = {},
                            onTaskInfo = {},
                        )
                    }
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-translation")).assertIsDisplayed()
        assertHasTranslations(recorded, languageTag, context)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
        fun languages(): List<String> = listOf(AppLocale.ENGLISH, AppLocale.SPANISH)
    }
}
