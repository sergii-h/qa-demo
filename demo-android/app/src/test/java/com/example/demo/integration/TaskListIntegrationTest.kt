package com.example.demo.integration

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.context.TaskTestContext
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.experimental.runners.Enclosed
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class TaskListIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun pullToRefresh() {
            runAsyncAction {
                onNodeWithTag(TestTags.TASK_LIST).performTouchInput {
                    swipeDown(
                        startY = top + height * 0.05f,
                        endY = top + height * 0.95f,
                        durationMillis = 400,
                    )
                }
            }
            assertIsNotDisplayed(TestTags.REFRESHING)
        }

        protected fun assertTaskListHasSize(expectedSize: Int) {
            waitUntilCondition(5_000) {
                taskTitleNodeCount() == expectedSize
            }
        }

        protected fun clickDeleteItem(id: String) {
            runAsyncAction { onNodeWithTag(TestTags.deleteButton(id)).performClick() }
        }

        protected fun openCreateForm() {
            runAsyncAction { onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick() }
            assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        }

        protected fun openDetailForm(taskId: String) {
            runAsyncAction { onNodeWithTag(TestTags.infoButton(taskId)).performClick() }
            assertIsDisplayed(TestTags.DESCRIPTION)
        }

        protected fun openEditForm(taskId: String) {
            runAsyncAction { onNodeWithTag(TestTags.editButton(taskId)).performClick() }
            assertIsDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
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

    @RunWith(RobolectricTestRunner::class)
    class TaskListDisplayIntegrationTests : Base() {

        @Test
        fun shouldRenderTaskListWithFetchedDataWhenTheListIsFirstShown() {
            // Given
            val firstContext = TaskTestContext(status = TaskStatus.TODO, priority = TaskPriority.LOW)
            val secondContext = TaskTestContext(status = TaskStatus.DONE, priority = TaskPriority.HIGH)

            mockServer.enqueueGetTasks(
                firstContext.createTaskResponse(),
                secondContext.createTaskResponse(),
            )

            // When
            launchApp()

            // Then
            assertThat(mockServer.getTasksRequestCount).isEqualTo(1)
            assertTaskListHasSize(2)
            assertIsDisplayed(TestTags.taskTitle(firstContext.id))
            assertIsDisplayed(TestTags.taskTitle(secondContext.id))
            assertTextEquals(TestTags.taskTitle(firstContext.id), firstContext.title)
            assertTextEquals(TestTags.taskTitle(secondContext.id), secondContext.title)
        }

        @Test
        fun shouldDisplayStatusPriorityTagsAndActionButtonsForEachTask() {
            // Given
            val firstContext = TaskTestContext(status = TaskStatus.TODO, priority = TaskPriority.LOW)
            val secondContext = TaskTestContext(status = TaskStatus.IN_PROGRESS, priority = TaskPriority.MEDIUM)
            val thirdContext = TaskTestContext(status = TaskStatus.DONE, priority = TaskPriority.HIGH)

            mockServer.enqueueGetTasks(
                firstContext.createTaskResponse(),
                secondContext.createTaskResponse(),
                thirdContext.createTaskResponse(),
            )

            // When
            launchApp()

            // Then
            assertTaskListHasSize(3)
            assertIsDisplayed(TestTags.statusTag(firstContext.status))
            assertIsDisplayed(TestTags.priorityTag(firstContext.priority))
            assertIsDisplayed(TestTags.statusTag(secondContext.status))
            assertIsDisplayed(TestTags.priorityTag(secondContext.priority))
            assertIsDisplayed(TestTags.statusTag(thirdContext.status))
            assertIsDisplayed(TestTags.priorityTag(thirdContext.priority))
            composeTestRule.onNodeWithTag(TestTags.infoButton(firstContext.id)).assertIsDisplayed()
            composeTestRule.onNodeWithTag(TestTags.editButton(firstContext.id)).assertIsDisplayed()
            composeTestRule.onNodeWithTag(TestTags.deleteButton(firstContext.id)).assertIsDisplayed()
            assertIsDisplayed(TestTags.infoButton(secondContext.id))
            assertIsDisplayed(TestTags.editButton(secondContext.id))
            assertIsDisplayed(TestTags.deleteButton(secondContext.id))
            assertIsDisplayed(TestTags.infoButton(thirdContext.id))
            assertIsDisplayed(TestTags.editButton(thirdContext.id))
            assertIsDisplayed(TestTags.deleteButton(thirdContext.id))
        }

        @Test
        fun shouldRenderEmptyListStateWhenTasksResponseIsEmpty() {
            // Given
            mockServer.enqueueGetTasks()

            // When
            launchApp()

            // Then
            assertTaskListHasSize(0)
            assertIsDisplayed(TestTags.EMPTY_TASKS)
            assertIsDisplayed(TestTags.ADD_TASK_BUTTON)
        }

        @Test
        fun shouldOpenCreateTaskFormFromListActions() {
            // Given
            val context = TaskTestContext()

            mockServer.enqueueGetTasks(context.createTaskResponse())
            launchApp()

            // When
            openCreateForm()

            // Then
            assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        }

        @Test
        fun shouldOpenTaskInfoFormFromListActions() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            // When
            openDetailForm(context.id)

            // Then
            assertIsDisplayed(TestTags.DESCRIPTION)
        }

        @Test
        fun shouldOpenTaskEditFormFromListActions() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
            launchApp()

            // When
            openEditForm(context.id)

            // Then
            assertIsDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
        }

        @Test
        fun shouldKeepCreateFlowAvailableWhenInitialGetTasksRequestFails() {
            // Given
            mockServer.enqueueGetTasksError(500)
            launchApp()

            // Then
            assertIsDisplayed(TestTags.EMPTY_TASKS)
            assertIsDisplayed(TestTags.ADD_TASK_BUTTON)

            // When
            openCreateForm()

            // Then
            assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        }

        @Test
        fun shouldHaveTranslationsForTaskList() {
            // Given
            val context = TaskTestContext(status = TaskStatus.TODO, priority = TaskPriority.LOW)

            mockServer.enqueueGetTasksForLanguageSwitch(context.createTaskResponse())
            launchApp()

            // When
            switchLanguage(LanguageOption.ES)

            // Then
            assertTaskListHasSize(1)
            assertTextEquals(TestTags.PAGE_TITLE, "Tareas")
            assertTextEquals(TestTags.statusTag(context.status), "Por hacer")
            assertTextEquals(TestTags.priorityTag(context.priority), "Baja")
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class PullToRefreshIntegrationTests : Base() {

        @Test
        fun shouldShowNewTaskWhenPullToRefreshReturnsUpdatedList() {
            // Given
            val firstContext = TaskTestContext()
            val secondContext = TaskTestContext()

            mockServer.enqueueGetTasks(firstContext.createTaskResponse())
            launchApp()

            assertTaskListHasSize(1)
            assertIsDisplayed(TestTags.taskTitle(firstContext.id))
            mockServer.enqueueGetTasks(
                firstContext.createTaskResponse(),
                secondContext.createTaskResponse(),
            )

            // When
            pullToRefresh()

            // Then
            assertTaskListHasSize(2)
            assertIsDisplayed(TestTags.taskTitle(firstContext.id))
            assertIsDisplayed(TestTags.taskTitle(secondContext.id))
        }

        @Test
        fun shouldKeepExistingTasksWhenPullToRefreshReturnsSameList() {
            // Given
            val context = TaskTestContext()

            mockServer.enqueueGetTasks(context.createTaskResponse())
            launchApp()

            assertTaskListHasSize(1)
            assertIsDisplayed(TestTags.taskTitle(context.id))
            mockServer.enqueueGetTasks(context.createTaskResponse())

            // When
            pullToRefresh()

            // Then
            assertTaskListHasSize(1)
            assertIsDisplayed(TestTags.taskTitle(context.id))
        }

        @Test
        fun shouldKeepExistingTasksWhenPullToRefreshFailsWithServerError() {
            // Given
            val firstContext = TaskTestContext()
            val secondContext = TaskTestContext()

            mockServer.enqueueGetTasks(
                firstContext.createTaskResponse(),
                secondContext.createTaskResponse(),
            )
            launchApp()

            assertTaskListHasSize(2)
            assertIsDisplayed(TestTags.taskTitle(firstContext.id))
            assertIsDisplayed(TestTags.taskTitle(secondContext.id))
            mockServer.enqueueGetTasksError(500)

            // When
            pullToRefresh()

            // Then
            assertTaskListHasSize(2)
            assertIsDisplayed(TestTags.taskTitle(firstContext.id))
            assertIsDisplayed(TestTags.taskTitle(secondContext.id))
            assertIsDisplayed(TestTags.ERROR_SNACKBAR)
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class DeleteTaskIntegrationTests : Base() {

        @Test
        fun shouldSendDeleteRequestWithSelectedTaskIdWhenDeleteIsTriggeredAndRemoveTaskFromListAfterSuccessfulDeleteResponse() {
            // Given
            val deleteContext = TaskTestContext()
            val keepContext = TaskTestContext()

            mockServer
                .enqueueGetTasks(
                    deleteContext.createTaskResponse(),
                    keepContext.createTaskResponse(),
                )
                .enqueueDeleteSuccess()
            launchApp()

            // When
            clickDeleteItem(deleteContext.id)

            // Then
            waitUntilCondition { mockServer.deletedTaskIds.contains(deleteContext.id) }
            assertThat(mockServer.deletedTaskIds).containsExactly(deleteContext.id)
            assertTaskListHasSize(1)
            assertIsDisplayed(TestTags.taskTitle(keepContext.id))
            assertThat(mockServer.getTasksRequestCount).isEqualTo(1)
        }

        @Test
        fun shouldKeepTaskInListWhenDeleteFailsWithHttp500() {
            // Given
            val deleteContext = TaskTestContext()
            val keepContext = TaskTestContext()

            mockServer
                .enqueueGetTasks(
                    deleteContext.createTaskResponse(),
                    keepContext.createTaskResponse(),
                )
                .enqueueDeleteError(500, "Delete failed")
            launchApp()

            // When
            clickDeleteItem(deleteContext.id)

            // Then
            waitUntilCondition { mockServer.deletedTaskIds.contains(deleteContext.id) }
            assertThat(mockServer.deletedTaskIds).containsExactly(deleteContext.id)
            assertTaskListHasSize(2)
            assertIsDisplayed(TestTags.taskTitle(deleteContext.id))
            assertIsDisplayed(TestTags.taskTitle(keepContext.id))
            assertIsDisplayed(TestTags.ERROR_SNACKBAR)
        }

        @Test
        fun shouldAllowDeleteRetryAfterFailureAndRemoveTaskWhenRetrySucceeds() {
            // Given
            val deleteContext = TaskTestContext()
            val keepContext = TaskTestContext()

            mockServer
                .enqueueGetTasks(
                    deleteContext.createTaskResponse(),
                    keepContext.createTaskResponse(),
                )
                .enqueueDeleteError(500, "Delete failed")
                .enqueueDeleteSuccess()
            launchApp()

            // When
            clickDeleteItem(deleteContext.id)
            waitUntilCondition { mockServer.deletedTaskIds.contains(deleteContext.id) }
            assertIsDisplayed(TestTags.ERROR_SNACKBAR)

            // And
            clickDeleteItem(deleteContext.id)

            // Then
            assertTaskListHasSize(1)
            assertIsDisplayed(TestTags.taskTitle(keepContext.id))
        }
    }
}
