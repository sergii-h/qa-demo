package com.example.demo.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.runAsyncAction
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class DemoNavHostTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository = mockk<TaskRepository>()

    @Test
    fun shouldNavigateToCreateScreenWhenFabClicked() {
        // Given
        coEvery { repository.getTasks() } returns emptyList()

        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag("language-switcher").assertIsDisplayed()

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            onNodeWithTag("add-task-button").performClick()
        }

        // Then
        composeTestRule.onNodeWithTag("modal-title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("create-task-title-input").assertIsDisplayed()
    }

    @Test
    fun shouldNavigateToEditScreenWhenEditClicked() {
        // Given
        coEvery { repository.getTasks() } returns listOf(TaskFixtures.sampleTask)
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask

        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            onNodeWithTag("edit-button-${"task-1"}").performClick()
        }

        // Then
        composeTestRule.onNodeWithTag("edit-task-title-input").assertIsDisplayed()
        composeTestRule.onNodeWithTag("modal-title").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save-button").performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldNavigateToDetailScreenWhenInfoClicked() {
        // Given
        coEvery { repository.getTasks() } returns listOf(TaskFixtures.sampleTask)
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } returns true

        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    DemoNavHost(repository = repository)
                }
            }
        }

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            onNodeWithTag("info-button-${"task-1"}").performClick()
        }

        // Then
        composeTestRule.onNodeWithTag("description").assertIsDisplayed()
        composeTestRule.onNodeWithTag("priority-tag-${TaskFixtures.sampleTask.priority.name}").assertIsDisplayed()
    }
}
