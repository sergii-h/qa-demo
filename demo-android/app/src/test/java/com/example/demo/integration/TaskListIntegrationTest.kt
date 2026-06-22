package com.example.demo.integration

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.support.IntegrationTasks
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskListIntegrationTest : IntegrationTestBase() {

    @Test
    fun shouldShowNewTaskWhenPullToRefreshReturnsUpdatedList() {
        // Given
        mockServer.enqueueGetTasks(IntegrationTasks.task("1", "First Task"))
        launchApp()
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "First Task"),
            IntegrationTasks.task("2", "Second Task"),
        )

        // When
        pullToRefresh()

        // Then
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldKeepExistingTasksWhenPullToRefreshReturnsSameList() {
        // Given
        val task = IntegrationTasks.task("1", "Stable Task")
        mockServer.enqueueGetTasks(task)
        launchApp()
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        mockServer.enqueueGetTasks(task)

        // When
        pullToRefresh()

        // Then
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepExistingTasksWhenPullToRefreshFailsWithServerError() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Keep Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Also Keep", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        launchApp()
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        mockServer.enqueueGetTasksError(500)

        // When
        pullToRefresh()

        // Then
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
    }

    @Test
    fun shouldRenderTaskListWithFetchedDataWhenListFirstShown() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Task Two", status = TaskStatus.IN_PROGRESS, priority = TaskPriority.MEDIUM),
            IntegrationTasks.task("3", "Task Three", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )

        // When
        launchApp()

        // Then
        assertTaskListHasSize(3)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("3")).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldDisplayStatusAndPriorityTagsForAllTaskVariants() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Task Two", status = TaskStatus.IN_PROGRESS, priority = TaskPriority.MEDIUM),
            IntegrationTasks.task("3", "Task Three", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )

        // When
        launchApp()

        // Then
        assertTaskListHasSize(3)
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
        mockServer.enqueueGetTasks(IntegrationTasks.task("10", "Modal Task", description = "Modal description"))

        // When
        launchApp()

        // Then
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.infoButton("10")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.editButton("10")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.deleteButton("10")).assertIsDisplayed()
    }

    @Test
    fun shouldRenderEmptyListWhenTasksResponseIsEmpty() {
        // Given
        mockServer.enqueueGetTasks()

        // When
        launchApp()

        // Then
        assertTaskListHasSize(0)
        composeTestRule.onNodeWithTag(TestTags.EMPTY_TASKS).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.infoButton("1")).assertDoesNotExist()
    }

    @Test
    fun shouldOpenCreateFlowFromAddTaskButton() {
        // Given
        mockServer.enqueueGetTasks(IntegrationTasks.task("10", "Modal Task", description = "Modal description"))
        launchApp()

        // When
        openCreateForm()

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldOpenDetailFlowFromInfoButton() {
        // Given
        val task = IntegrationTasks.task("10", "Modal Task", description = "Modal description")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()

        // When
        openDetail("10")

        // Then
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun shouldOpenEditFlowFromEditButton() {
        // Given
        val task = IntegrationTasks.task("10", "Modal Task", description = "Modal description")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        launchApp()

        // When
        openEditForm("10")

        // Then
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldRemoveTaskFromListWhenDeleteSucceeds() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains("1")
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldUpdateListWithoutFullClientReloadWhenDeleteSucceeds() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains("1")
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsWithServerError() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        mockServer.enqueueDeleteError(500, "Delete failed")
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains("1")
        }

        // Then
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        commonAssertions.assertErrorSnackbarDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsDueToNetworkError() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        mockServer.enqueueDeleteNetworkFailure()
        launchApp()
        assertTaskListHasSize(2)

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains("1")
        }

        // Then
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
        commonAssertions.assertErrorSnackbarDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly("1")
    }

    @Test
    fun shouldRemoveTaskWhenDeleteRetrySucceeds() {
        // Given
        mockServer.enqueueGetTasks(
            IntegrationTasks.task("1", "Delete Me", status = TaskStatus.TODO, priority = TaskPriority.LOW),
            IntegrationTasks.task("2", "Keep Me", status = TaskStatus.DONE, priority = TaskPriority.HIGH),
        )
        mockServer.enqueueDeleteError(500, "Delete failed")
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }
        commonAssertions.assertErrorSnackbarDisplayed()
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton("1")).performClick()
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("2")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetFails() {
        // Given
        mockServer.enqueueGetTasksError(500)

        // When
        launchApp()
        openCreateForm()

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetRequestIsRejected() {
        // Given
        mockServer.enqueueGetTasksNetworkFailure()

        // When
        launchApp()

        // Then
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
    }

    @Test
    fun shouldShowSpanishTaskListStringsWhenEsSelected() {
        // Given
        val task = IntegrationTasks.task("1", "Task One", status = TaskStatus.TODO, priority = TaskPriority.LOW)
        mockServer.enqueueGetTasksForLanguageSwitch(task)
        launchApp()

        // When
        switchLanguage(LanguageOption.ES)

        // Then
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.PAGE_TITLE).assertTextEquals("Tareas")
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO))
            .performScrollTo()
            .assertTextEquals("Por hacer")
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.LOW))
            .performScrollTo()
            .assertTextEquals("Baja")
    }

    private fun pullToRefresh() {
        runAsyncAction {
            onNodeWithTag(TestTags.TASK_LIST).performTouchInput {
                swipeDown(
                    startY = top + height * 0.05f,
                    endY = top + height * 0.95f,
                    durationMillis = 400,
                )
            }
        }
        waitUntilRefreshFinished()
    }

    private fun assertTaskListHasSize(expectedSize: Int) {
        assertThat(taskTitleNodeCount()).isEqualTo(expectedSize)
    }

    private fun waitUntilTaskListHasSize(expectedSize: Int, timeoutMillis: Long = 5_000) {
        waitUntilCondition(timeoutMillis) {
            taskTitleNodeCount() == expectedSize
        }
    }

    private val taskTitleMatcher = SemanticsMatcher("task-title prefix") { node ->
        val config = node.config
        config.contains(SemanticsProperties.TestTag) &&
            config[SemanticsProperties.TestTag].startsWith("task-title-")
    }

    private fun taskTitleNodeCount(): Int =
        composeTestRule.onAllNodes(taskTitleMatcher, useUnmergedTree = true)
            .fetchSemanticsNodes()
            .size

}
