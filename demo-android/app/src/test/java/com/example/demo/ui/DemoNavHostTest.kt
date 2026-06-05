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
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.testing.waitUntilTagExists
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

        composeTestRule.setContent {
            DemoComposeTestTheme {
                DemoNavHost(repository = repository)
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.LANGUAGE_SWITCHER).assertIsDisplayed()

        // When
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldNavigateToEditScreenWhenEditClicked() {
        // Given
        coEvery { repository.getTasks() } returns listOf(TaskFixtures.sampleTask)
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask

        composeTestRule.setContent {
            DemoComposeTestTheme {
                DemoNavHost(repository = repository)
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // When
        composeTestRule.onNodeWithTag(TestTags.editButton("task-1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.EDIT_TASK_TITLE_INPUT)
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldNavigateToDetailScreenWhenInfoClicked() {
        // Given
        coEvery { repository.getTasks() } returns listOf(TaskFixtures.sampleTask)
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } returns true

        composeTestRule.setContent {
            DemoComposeTestTheme {
                DemoNavHost(repository = repository)
            }
        }
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // When
        composeTestRule.onNodeWithTag(TestTags.infoButton("task-1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.DESCRIPTION)
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskFixtures.sampleTask.priority)).assertIsDisplayed()
    }
}
