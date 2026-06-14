package com.example.demo.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.support.FakeTaskApi
import com.example.demo.integration.support.IntegrationTasks
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTaskIntegrationTest : IntegrationTestBase() {

    @Test
    fun shouldUpdateTaskWithAllFieldsAndRefreshList() {
        // Given
        val originalTask = IntegrationTasks.task(
            id = "task-201",
            title = "Existing title",
            description = "Existing description",
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
        )
        val updatedTask = IntegrationTasks.task(
            id = "task-201",
            title = "Updated title",
            description = "Updated description",
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
        )
        fakeApi.enqueueGetTasks(originalTask)
        fakeApi.enqueueGetTask(originalTask)
        fakeApi.enqueueUpdateTask(updatedTask)
        fakeApi.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-201")

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Updated description")
        selectStatus(TaskStatus.DONE)
        selectPriority(TaskPriority.HIGH)
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-201",
                TaskRequest(
                    "Updated title",
                    "Updated description",
                    TaskStatus.DONE,
                    TaskPriority.HIGH,
                ),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-201")).assertIsDisplayed()
    }

    @Test
    fun shouldUpdateTaskAndRefreshList() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title", description = "Notes")
        fakeApi.enqueueGetTasks(originalTask)
        fakeApi.enqueueGetTask(originalTask)
        fakeApi.enqueueUpdateTask(updatedTask)
        fakeApi.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-1",
                TaskRequest("Updated title", "Notes", TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldCloseEditFlowWithoutSavingChanges() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        fakeApi.enqueueGetTasks(task)
        fakeApi.enqueueGetTask(task)
        fakeApi.enqueueGetTask(task)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Unsaved title")

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.CLOSE_BUTTON).performClick()
        }
        openEditForm("task-1")

        // Then
        assertThat(fakeApi.updateTaskRequests).isEmpty()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldProceedWithSaveAfterInvalidTitleCorrected() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Corrected title")
        fakeApi.enqueueGetTasks(originalTask)
        fakeApi.enqueueGetTask(originalTask)
        fakeApi.enqueueUpdateTask(updatedTask)
        fakeApi.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("a".repeat(101))
        submitEditForm(expectList = false)
        commonAssertions.assertTitleError("Title must not exceed 100 characters")
        assertThat(fakeApi.updateTaskRequests).isEmpty()

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Corrected title")
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-1",
                TaskRequest("Corrected title", null, TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayGenericErrorWhenPutFailsWithServerError() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title")
        fakeApi.enqueueGetTasks(task)
        fakeApi.enqueueGetTask(task)
        fakeApi.enqueueUpdateTaskError(500)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditForm(expectList = false)

        // Then
        commonAssertions.assertTitleError("Request failed (500)")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldRetryAndSaveAfterInitialPutFailure() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title")
        fakeApi.enqueueGetTasks(originalTask)
        fakeApi.enqueueGetTask(originalTask)
        fakeApi.enqueueUpdateTaskError(500)
        fakeApi.enqueueUpdateTask(updatedTask)
        fakeApi.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        val expectedRequest = FakeTaskApi.RecordedUpdate(
            "task-1",
            TaskRequest("Updated title", null, TaskStatus.TODO, TaskPriority.MEDIUM),
        )
        submitEditForm(expectList = false)
        commonAssertions.assertTitleError("Request failed (500)")
        assertThat(fakeApi.updateTaskRequests).containsExactly(expectedRequest)
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(expectedRequest, expectedRequest)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepEditFlowAvailableWhenInitialGetFails() {
        // Given
        val listTask = IntegrationTasks.task("task-1", "Original title")
        fakeApi.enqueueGetTasks(listTask)
        fakeApi.enqueueGetTaskError(500)
        launchApp()

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.editButton("task-1")).performClick()
        }

        // Then
        commonAssertions.assertLoadErrorDisplayed()
    }

    @Test
    fun shouldUpdateTaskWithRemovedDescriptionAndRefreshList() {
        // Given
        val originalTask = IntegrationTasks.task(
            id = "task-210",
            title = "Task with description",
            description = "Description to remove",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.MEDIUM,
        )
        val updatedTask = IntegrationTasks.task(
            id = "task-210",
            title = "Task with description",
            description = null,
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.MEDIUM,
        )
        fakeApi.enqueueGetTasks(originalTask)
        fakeApi.enqueueGetTask(originalTask)
        fakeApi.enqueueUpdateTask(updatedTask)
        fakeApi.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-210")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextClearance()

        // When
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-210",
                TaskRequest(
                    "Task with description",
                    null,
                    TaskStatus.IN_PROGRESS,
                    TaskPriority.MEDIUM,
                ),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-210")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayErrorWhenPutRequestIsRejected() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        fakeApi.enqueueGetTasks(task)
        fakeApi.enqueueGetTask(task)
        fakeApi.enqueueUpdateTaskNetworkFailure()
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditForm(expectList = false)

        // Then
        commonAssertions.assertTitleError("Network request failed")
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-1",
                TaskRequest("Updated title", "Notes", TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldShowSpanishEditFlowStringsWhenEsSelected() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        fakeApi.enqueueGetTasksForLanguageSwitch(task)
        fakeApi.enqueueGetTask(task)
        launchApp()
        switchLanguage(LanguageOption.ES)

        // When
        openEditForm("task-1")

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertTextEquals("Editar tarea")
        commonAssertions.assertFieldLabel(TestTags.FIELD_TITLE_LABEL, "Título *")
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertTextEquals("Guardar")
    }

    private fun submitEditForm(expectList: Boolean = true) {
        runAsyncAction {
            onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick()
        }
        if (expectList) {
            composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
            flushAsyncWork()
        }
    }

    private fun openEditForm(taskId: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.editButton(taskId)).performClick()
        }
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    private fun selectPriority(priority: TaskPriority) {
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).performScrollTo().performClick()
        runAsyncAction {
            onNodeWithTag(TestTags.priorityDropdownOption(priority)).performClick()
        }
    }

    private fun selectStatus(status: TaskStatus) {
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).performScrollTo().performClick()
        runAsyncAction {
            onNodeWithTag(TestTags.statusDropdownOption(status)).performClick()
        }
    }
}
