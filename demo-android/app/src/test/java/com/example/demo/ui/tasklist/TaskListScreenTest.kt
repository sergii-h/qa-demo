package com.example.demo.ui.tasklist

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.HttpExceptionFactory
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TaskListScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository = mockk<TaskRepository>()

    @Test
    fun shouldShowEmptyStateWhenNoTasksReturned() {
        // Given
        coEvery { repository.getTasks() } returns emptyList()

        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = {},
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.EMPTY_TASKS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).assertIsDisplayed()
    }

    @Test
    fun shouldShowTaskTitleWhenTasksLoaded() {
        // Given
        coEvery { repository.getTasks() } returns listOf(TaskFixtures.sampleTask)

        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = {},
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldInvokeCreateCallbackWhenFabClicked() {
        // Given
        coEvery { repository.getTasks() } returns emptyList()
        var createClicked = false

        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = { createClicked = true },
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // When
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()

        // Then
        assertThat(createClicked).isTrue()
    }

    @Test
    fun shouldShowTaskRowActionsAndTagsWhenTasksLoaded() {
        // Given
        val task = TaskFixtures.sampleTask
        coEvery { repository.getTasks() } returns listOf(task)

        // When
        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = {},
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle(task.id)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.statusTag(task.status)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(task.priority)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.infoButton(task.id)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.editButton(task.id)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.deleteButton(task.id)).assertIsDisplayed()
    }

    @Test
    fun shouldInvokeDeleteWhenDeleteButtonClicked() {
        // Given
        val task = TaskFixtures.sampleTask
        coEvery { repository.getTasks() } returns listOf(task)
        coEvery { repository.deleteTask(task.id) } returns Unit

        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = {},
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton(task.id)).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        coVerify { repository.deleteTask(task.id) }
    }

    @Test
    fun shouldInvokeCreateCallbackWhenFabClickedAfterLoadFails() {
        // Given
        coEvery { repository.getTasks() } throws HttpExceptionFactory.create(500)
        var createClicked = false

        composeTestRule.setContent {
            DemoComposeTestTheme {
                TaskListScreen(
                    repository = repository,
                    onCreateTask = { createClicked = true },
                    onEditTask = {},
                    onTaskInfo = {}
                )
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // When
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()

        // Then
        assertThat(createClicked).isTrue()
    }
}
