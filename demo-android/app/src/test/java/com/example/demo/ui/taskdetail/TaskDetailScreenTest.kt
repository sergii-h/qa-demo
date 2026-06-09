package com.example.demo.ui.taskdetail

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.DemoComposeTestTheme
import com.example.demo.testing.HttpExceptionFactory
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.example.demo.testing.runAsyncAction
import com.example.demo.testing.waitUntilTextExists
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
class TaskDetailScreenTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val composeTestRule = createComposeRule()

    private val repository = mockk<TaskRepository>()

    @Test
    fun shouldShowTaskDetailsWhenLoadSucceeds() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } returns true

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskFixtures.sampleTask.status)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskFixtures.sampleTask.priority)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.VALID).assertExists()
    }

    @Test
    fun shouldShowNotAvailableWhenDatesAreMissing() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask.copy(
            createdDate = null,
            updatedDate = "   ",
        )
        coEvery { repository.isValid("task-1") } returns true

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATED_DATE).assertTextEquals("N/A")
        composeTestRule.onNodeWithTag(TestTags.UPDATED_DATE).assertTextEquals("N/A")
    }

    @Test
    fun shouldShowRawValueWhenDateParseFails() {
        // Given
        val invalidDate = "not-an-instant"
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask.copy(
            createdDate = invalidDate,
        )
        coEvery { repository.isValid("task-1") } returns true

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATED_DATE).assertTextEquals(invalidDate)
    }

    @Test
    fun shouldShowErrorWhenLoadFails() {
        // Given
        coEvery { repository.getTask("task-1") } throws HttpExceptionFactory.create(404)
        coEvery { repository.isValid("task-1") } returns false

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertIsDisplayed()
    }

    @Test
    fun shouldShowNotValidIndicatorWhenTaskIsInvalid() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } returns false

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertExists()
        composeTestRule.onNodeWithTag(TestTags.VALID).assertDoesNotExist()
    }

    @Test
    fun shouldShowValidatedLabelWhenTaskLoaded() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } returns true

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.waitUntilTextExists("Validated")
        composeTestRule.onNodeWithText("Validated").assertExists()
    }

    @Test
    fun shouldShowNoDescriptionWhenDescriptionIsEmpty() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask.copy(description = null)
        coEvery { repository.isValid("task-1") } returns true

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertTextEquals("No description")
    }

    @Test
    fun shouldShowTaskDetailsWhenValidationRequestFails() {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.isValid("task-1") } throws HttpExceptionFactory.create(500)

        // When
        composeTestRule.runAsyncAction(mainDispatcherRule.dispatcher) {
            setContent {
                DemoComposeTestTheme {
                    TaskDetailScreen(
                        repository = repository,
                        taskId = "task-1",
                        onBack = {}
                    )
                }
            }
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertTextEquals(TaskFixtures.sampleTask.description!!)
        composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertExists()
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertDoesNotExist()
    }
}
