package com.example.demo.ui.taskform

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.testing.waitUntilTagExists
import com.example.demo.ui.TestTags
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
        composeTestRule.setContent {
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
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.MODAL_TITLE)
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldShowLoadedTaskWhenEditModeSucceeds() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask

        // When
        composeTestRule.setContent {
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
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.EDIT_TASK_TITLE_INPUT)
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertIsDisplayed()
    }
}
