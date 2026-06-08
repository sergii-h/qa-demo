package com.example.demo.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.testing.waitUntilTagExists
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskListIntegrationTest : IntegrationTestBase() {

    @Test
    fun shouldRenderTaskListWithFetchedDataWhenListFirstShown() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Task Two", status = TaskStatus.IN_PROGRESS, priority = TaskPriority.MEDIUM),
            IntegrationTasks.task("3", "Task Three", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )

        // When
        launchApp()

        // Then
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("3")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldDisplayStatusAndPriorityTagsForAllTaskVariants() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Task Two", status = TaskStatus.IN_PROGRESS, priority = TaskPriority.MEDIUM),
            IntegrationTasks.task("3", "Task Three", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )

        // When
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // Then
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.LOW)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.IN_PROGRESS)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.MEDIUM)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.DONE)).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.HIGH)).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldRenderActionButtonsForEachTask() {
        // Given
        enqueueTasks(IntegrationTasks.task("10", "Modal Task", description = "Modal description"))

        // When
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("10"))

        // Then
        composeTestRule.onNodeWithTag(TestTags.infoButton("10")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.editButton("10")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.deleteButton("10")).assertIsDisplayed()
    }

    @Test
    fun shouldRenderEmptyListWhenTasksResponseIsEmpty() {
        // Given
        enqueueTasks()

        // When
        launchApp()

        // Then
        composeTestRule.waitUntilTagExists(TestTags.EMPTY_TASKS)
        composeTestRule.onNodeWithTag(TestTags.EMPTY_TASKS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.infoButton("1")).assertDoesNotExist()
    }

    @Test
    fun shouldOpenCreateFlowFromAddTaskButton() {
        // Given
        enqueueTasks(IntegrationTasks.task("10", "Modal Task", description = "Modal description"))
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("10"))

        // When
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldOpenDetailFlowFromInfoButton() {
        // Given
        val task = IntegrationTasks.task("10", "Modal Task", description = "Modal description")
        enqueueTasks(task)
        enqueueGetTask(task)
        enqueueValidation(true)
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("10"))

        // When
        composeTestRule.onNodeWithTag(TestTags.infoButton("10")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.DESCRIPTION)
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun shouldOpenEditFlowFromEditButton() {
        // Given
        val task = IntegrationTasks.task("10", "Modal Task", description = "Modal description")
        enqueueTasks(task)
        enqueueGetTask(task)
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("10"))

        // When
        composeTestRule.onNodeWithTag(TestTags.editButton("10")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntilTagExists(TestTags.EDIT_TASK_TITLE_INPUT)
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldRemoveTaskFromListWhenDeleteSucceeds() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        enqueueDeleteSuccess()
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.taskTitle("1"))
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        assertThat(fakeApi.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldUpdateListWithoutFullClientReloadWhenDeleteSucceeds() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        enqueueDeleteSuccess()
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.taskTitle("1"))
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        assertThat(
            composeTestRule.onAllNodesWithTag(TestTags.taskTitle("2")).fetchSemanticsNodes()
        ).hasSize(1)
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsWithServerError() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        enqueueDeleteError(500, "Delete failed")
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        assertThat(fakeApi.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsDueToNetworkError() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        enqueueDeleteNetworkFailure()
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        assertThat(fakeApi.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldRemoveTaskWhenDeleteRetrySucceeds() {
        // Given
        enqueueTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        enqueueDeleteError(500, "Delete failed")
        enqueueDeleteSuccess()
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))

        // When
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        composeTestRule.onNodeWithTag(TestTags.deleteButton("1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.taskTitle("1"))
                .fetchSemanticsNodes().isEmpty()
        }
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetFails() {
        // Given
        enqueueGetTasksError(500)

        // When
        launchApp()

        // Then
        composeTestRule.waitUntilTagExists(TestTags.ADD_TASK_BUTTON)
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetRequestIsRejected() {
        // Given
        enqueueGetTasksNetworkFailure()

        // When
        launchApp()

        // Then
        composeTestRule.waitUntilTagExists(TestTags.ADD_TASK_BUTTON)
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
    }

    @Test
    fun shouldShowSpanishTaskListStringsWhenEsSelected() {
        // Given
        val task = IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW)
        enqueueTasksForLanguageSwitch(task)
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.LANGUAGE_SWITCHER)

        // When
        switchToSpanish()

        // Then
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("1"))
        composeTestRule.onNodeWithTag(TestTags.PAGE_TITLE).assertTextEquals("Tareas")
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO))
            .performScrollTo()
            .assertTextEquals("Por hacer")
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.LOW))
            .performScrollTo()
            .assertTextEquals("Baja")
    }
}
