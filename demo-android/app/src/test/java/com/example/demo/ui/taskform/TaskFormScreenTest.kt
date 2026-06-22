package com.example.demo.ui.taskform

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.performTextInput
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.runAsyncAction
import com.example.demo.ui.TestTags
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TaskFormScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository = mockk<TaskRepository>()

    @Test
    fun shouldShowCreateFormWhenCreateMode() {
        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.CREATE,
                        taskId = null,
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldShowLoadedTaskWhenEditModeSucceeds() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.EDIT,
                        taskId = "task-1",
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldDisableCreateButtonWhenTitleIsBlank() {
        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.CREATE,
                        taskId = null,
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun shouldDisableSaveButtonWhenTitleIsBlankInEditMode() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask.copy(title = "")

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.EDIT,
                        taskId = "task-1",
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertIsNotEnabled()
    }

    @Test
    fun shouldShowDefaultStatusAndPriorityWhenCreateMode() {
        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.CREATE,
                        taskId = null,
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).assertTextContains("To Do")
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).assertTextContains("Medium")
    }

    @Test
    fun shouldShowSavingSpinnerWhenCreateSubmitted() {
        // Given
        coEvery { repository.createTask(any()) } coAnswers {
            delay(1_000)
            TaskFixtures.sampleTask
        }

        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskFormScreen(
                        repository = repository,
                        mode = TaskFormMode.CREATE,
                        taskId = null,
                        onBack = {},
                        onSaved = {}
                    )
                }
            }
        }

        // When
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextInput("New task")
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick()
        mainDispatcherRule.dispatcher.scheduler.runCurrent()
        composeTestRule.waitForIdle()

        // Then
        composeTestRule.onNodeWithTag(TestTags.LOADING_SPINNER).assertIsDisplayed()
    }
}
