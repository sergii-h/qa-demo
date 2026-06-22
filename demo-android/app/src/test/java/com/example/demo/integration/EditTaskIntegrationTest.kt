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
import com.example.demo.integration.support.IntegrationMockServer
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
        mockServer.enqueueGetTasks(originalTask)
        mockServer.enqueueGetTask(originalTask)
        mockServer.enqueueUpdateTask(updatedTask)
        mockServer.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-201")

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Updated description")
        selectStatus(TaskStatus.DONE)
        selectPriority(TaskPriority.HIGH)
        submitEditFormAndExpectList()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(
            IntegrationMockServer.RecordedUpdate(
                "task-201",
                TaskRequest(
                    "Updated title",
                    "Updated description",
                    TaskStatus.DONE,
                    TaskPriority.HIGH,
                ),
            ),
        )
        waitUntilTaskTitleVisible("task-201")
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-201")).assertIsDisplayed()
    }

    @Test
    fun shouldUpdateTaskAndRefreshList() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title", description = "Notes")
        mockServer.enqueueGetTasks(originalTask)
        mockServer.enqueueGetTask(originalTask)
        mockServer.enqueueUpdateTask(updatedTask)
        mockServer.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")
        submitEditFormAndExpectList()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(
            IntegrationMockServer.RecordedUpdate(
                "task-1",
                TaskRequest("Updated title", "Notes", TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        waitUntilTaskTitleVisible("task-1")
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldCloseEditFlowWithoutSavingChanges() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueGetTask(task)
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
        assertThat(mockServer.updateTaskRequests).isEmpty()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldProceedWithSaveAfterInvalidTitleCorrected() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Corrected title")
        mockServer.enqueueGetTasks(originalTask)
        mockServer.enqueueGetTask(originalTask)
        mockServer.enqueueUpdateTask(updatedTask)
        mockServer.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("a".repeat(101))
        submitEditFormAndStayOnForm()
        commonAssertions.assertTitleError("Title must not exceed 100 characters")
        assertThat(mockServer.updateTaskRequests).isEmpty()

        // When
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Corrected title")
        submitEditFormAndExpectList()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(
            IntegrationMockServer.RecordedUpdate(
                "task-1",
                TaskRequest("Corrected title", null, TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        waitUntilTaskTitleVisible("task-1")
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayGenericErrorWhenPutFailsWithServerError() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueUpdateTaskError(500)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditFormAndStayOnForm()

        // Then
        commonAssertions.assertSaveError("Request failed (500)")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldRetryAndSaveAfterInitialPutFailure() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title")
        mockServer.enqueueGetTasks(originalTask)
        mockServer.enqueueGetTask(originalTask)
        mockServer.enqueueUpdateTaskError(500)
        mockServer.enqueueUpdateTask(updatedTask)
        mockServer.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        val expectedRequest = IntegrationMockServer.RecordedUpdate(
            "task-1",
            TaskRequest("Updated title", null, TaskStatus.TODO, TaskPriority.MEDIUM),
        )
        submitEditFormAndStayOnForm()
        commonAssertions.assertSaveError("Request failed (500)")
        assertThat(mockServer.updateTaskRequests).containsExactly(expectedRequest)
        submitEditFormAndExpectList()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(expectedRequest, expectedRequest)
        waitUntilTaskTitleVisible("task-1")
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepEditFlowAvailableWhenInitialGetFails() {
        // Given
        val listTask = IntegrationTasks.task("task-1", "Original title")
        mockServer.enqueueGetTasks(listTask)
        mockServer.enqueueGetTaskError(500)
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
        mockServer.enqueueGetTasks(originalTask)
        mockServer.enqueueGetTask(originalTask)
        mockServer.enqueueUpdateTask(updatedTask)
        mockServer.enqueueGetTasks(updatedTask)
        launchApp()
        openEditForm("task-210")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextClearance()

        // When
        submitEditFormAndExpectList()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(
            IntegrationMockServer.RecordedUpdate(
                "task-210",
                TaskRequest(
                    "Task with description",
                    null,
                    TaskStatus.IN_PROGRESS,
                    TaskPriority.MEDIUM,
                ),
            ),
        )
        waitUntilTaskTitleVisible("task-210")
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-210")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayErrorWhenPutRequestIsRejected() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueUpdateTaskNetworkFailure()
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditFormAndStayOnForm()

        // Then
        commonAssertions.assertSaveError("End of input")
        assertThat(mockServer.updateTaskRequests).containsExactly(
            IntegrationMockServer.RecordedUpdate(
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
        mockServer.enqueueGetTasksForLanguageSwitch(task)
        mockServer.enqueueGetTask(task)
        launchApp()
        switchLanguage(LanguageOption.ES)

        // When
        openEditForm("task-1")

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertTextEquals("Editar tarea")
        commonAssertions.assertFieldLabel(TestTags.FIELD_TITLE_LABEL, "Título *")
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertTextEquals("Guardar")
    }

    private fun submitEditFormAndExpectList() {
        runAsyncAction {
            onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick()
        }
        waitUntilEditFormClosed()
        waitUntilListLoaded()
    }

    private fun submitEditFormAndStayOnForm() {
        runAsyncAction {
            onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick()
        }
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
