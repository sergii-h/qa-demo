package com.example.demo.integration

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
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
        val firstTask = Task(
            id = "1",
            title = "First Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        val secondTask = Task(
            id = "2",
            title = "Second Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(firstTask)
        launchApp()
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("1")).assertIsDisplayed()
        mockServer.enqueueGetTasks(firstTask, secondTask)

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
        val task = Task(
            id = "1",
            title = "Stable Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
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
        val firstTask = Task(
            id = "1",
            title = "Keep Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val secondTask = Task(
            id = "2",
            title = "Also Keep",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(firstTask, secondTask)
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
        mockServer.enqueueGetTasks(*tagVariantTasks.toTypedArray())

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
        mockServer.enqueueGetTasks(*tagVariantTasks.toTypedArray())

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
        val task = Task(
            id = "10",
            title = "Modal Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(task)

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
        val task = Task(
            id = "10",
            title = "Modal Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(task)
        launchApp()

        // When
        openCreateForm()

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldOpenDetailFlowFromInfoButton() {
        // Given
        val task = Task(
            id = "10",
            title = "Modal Task",
            description = "Modal description",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()

        // When
        openDetail(task.id)

        // Then
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertIsDisplayed()
    }

    @Test
    fun shouldOpenEditFlowFromEditButton() {
        // Given
        val task = Task(
            id = "10",
            title = "Modal Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        launchApp()

        // When
        openEditForm(task.id)

        // Then
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldRemoveTaskFromListWhenDeleteSucceeds() {
        // Given
        val deleteTask = Task(
            id = "1",
            title = "Delete Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val keepTask = Task(
            id = "2",
            title = "Keep Me",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(deleteTask, keepTask)
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains(deleteTask.id)
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle(keepTask.id)).assertIsDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly(deleteTask.id)
    }

    @Test
    fun shouldUpdateListWithoutFullClientReloadWhenDeleteSucceeds() {
        // Given
        val deleteTask = Task(
            id = "1",
            title = "Delete Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val keepTask = Task(
            id = "2",
            title = "Keep Me",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(deleteTask, keepTask)
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains(deleteTask.id)
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle(keepTask.id)).assertIsDisplayed()
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsWithServerError() {
        // Given
        val deleteTask = Task(
            id = "1",
            title = "Delete Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val keepTask = Task(
            id = "2",
            title = "Keep Me",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(deleteTask, keepTask)
        mockServer.enqueueDeleteError(500, "Delete failed")
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }
        waitUntilCondition {
            mockServer.deletedTaskIds.contains(deleteTask.id)
        }

        // Then
        assertTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle(deleteTask.id)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle(keepTask.id)).assertIsDisplayed()
        commonAssertions.assertErrorSnackbarDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly(deleteTask.id)
    }

    @Test
    fun shouldKeepTaskInListWhenDeleteFailsDueToNetworkError() {
        // Given
        val deleteTask = Task(
            id = "1",
            title = "Delete Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val keepTask = Task(
            id = "2",
            title = "Keep Me",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(deleteTask, keepTask)
        mockServer.enqueueDeleteNetworkFailure()
        launchApp()
        assertTaskListHasSize(2)

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }

        // Then
        commonAssertions.assertErrorSnackbarDisplayed()
        waitUntilTaskListHasSize(2)
        composeTestRule.onNodeWithTag(TestTags.taskTitle(deleteTask.id)).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.taskTitle(keepTask.id)).assertIsDisplayed()
        assertThat(mockServer.deletedTaskIds).containsExactly(deleteTask.id)
    }

    @Test
    fun shouldRemoveTaskWhenDeleteRetrySucceeds() {
        // Given
        val deleteTask = Task(
            id = "1",
            title = "Delete Me",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
        val keepTask = Task(
            id = "2",
            title = "Keep Me",
            description = null,
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
            createdDate = null,
            updatedDate = null,
        )
        mockServer.enqueueGetTasks(deleteTask, keepTask)
        mockServer.enqueueDeleteError(500, "Delete failed")
        mockServer.enqueueDeleteSuccess()
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }
        commonAssertions.assertErrorSnackbarDisplayed()
        runAsyncAction {
            onNodeWithTag(TestTags.deleteButton(deleteTask.id)).performClick()
        }

        // Then
        waitUntilTaskListHasSize(1)
        assertTaskListHasSize(1)
        composeTestRule.onNodeWithTag(TestTags.taskTitle(keepTask.id)).assertIsDisplayed()
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
        val task = Task(
            id = "1",
            title = "Task One",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.LOW,
            createdDate = null,
            updatedDate = null,
        )
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

    companion object {
        private val tagVariantTasks = listOf(
            Task(
                id = "1",
                title = "Task One",
                description = null,
                status = TaskStatus.TODO,
                priority = TaskPriority.LOW,
                createdDate = null,
                updatedDate = null,
            ),
            Task(
                id = "2",
                title = "Task Two",
                description = null,
                status = TaskStatus.IN_PROGRESS,
                priority = TaskPriority.MEDIUM,
                createdDate = null,
                updatedDate = null,
            ),
            Task(
                id = "3",
                title = "Task Three",
                description = null,
                status = TaskStatus.DONE,
                priority = TaskPriority.HIGH,
                createdDate = null,
                updatedDate = null,
            ),
        )
    }
}
