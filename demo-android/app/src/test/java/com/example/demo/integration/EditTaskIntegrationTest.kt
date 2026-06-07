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
import com.example.demo.testing.advanceComposeCoroutineIdle
import com.example.demo.testing.waitUntilTagExists
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
        enqueueTasks(originalTask)
        enqueueGetTask(originalTask)
        enqueueUpdateTask(updatedTask)
        enqueueTasks(updatedTask)
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
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-201"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-201")).assertIsDisplayed()
    }

    @Test
    fun shouldUpdateTaskAndRefreshList() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title", description = "Notes")
        enqueueTasks(originalTask)
        enqueueGetTask(originalTask)
        enqueueUpdateTask(updatedTask)
        enqueueTasks(updatedTask)
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
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-1"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldCloseEditFlowWithoutSavingChanges() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        enqueueTasks(task)
        enqueueGetTask(task)
        enqueueGetTask(task)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Unsaved title")

        // When
        composeTestRule.onNodeWithTag(TestTags.CLOSE_BUTTON).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        composeTestRule.onNodeWithTag(TestTags.editButton("task-1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        assertThat(fakeApi.updateTaskRequests).isEmpty()
        composeTestRule.waitUntilTagExists(TestTags.EDIT_TASK_TITLE_INPUT)
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldProceedWithSaveAfterInvalidTitleCorrected() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Corrected title")
        enqueueTasks(originalTask)
        enqueueGetTask(originalTask)
        enqueueUpdateTask(updatedTask)
        enqueueTasks(updatedTask)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("a".repeat(101))
        submitEditForm(expectList = false)
        assertTitleError("Title must not exceed 100 characters")
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
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-1"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayGenericErrorWhenPutFailsWithServerError() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title")
        enqueueTasks(task)
        enqueueGetTask(task)
        enqueueUpdateTaskError(500)
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditForm(expectList = false)

        // Then
        assertTitleError("Request failed (500)")
        assertThat(fakeApi.updateTaskRequests).containsExactly(
            FakeTaskApi.RecordedUpdate(
                "task-1",
                TaskRequest("Updated title", null, TaskStatus.TODO, TaskPriority.MEDIUM),
            ),
        )
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldRetryAndSaveAfterInitialPutFailure() {
        // Given
        val originalTask = IntegrationTasks.task("task-1", "Original title")
        val updatedTask = IntegrationTasks.task("task-1", "Updated title")
        enqueueTasks(originalTask)
        enqueueGetTask(originalTask)
        enqueueUpdateTaskError(500)
        enqueueUpdateTask(updatedTask)
        enqueueTasks(updatedTask)
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
        assertTitleError("Request failed (500)")
        assertThat(fakeApi.updateTaskRequests).containsExactly(expectedRequest)
        submitEditForm()

        // Then
        assertThat(fakeApi.updateTaskRequests).containsExactly(expectedRequest, expectedRequest)
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-1"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepEditFlowAvailableWhenInitialGetFails() {
        // Given
        val listTask = IntegrationTasks.task("task-1", "Original title")
        enqueueTasks(listTask)
        enqueueGetTaskError(500)
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-1"))
        composeTestRule.onNodeWithTag(TestTags.editButton("task-1")).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)

        // Then
        assertLoadErrorDisplayed()
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
        enqueueTasks(originalTask)
        enqueueGetTask(originalTask)
        enqueueUpdateTask(updatedTask)
        enqueueTasks(updatedTask)
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
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-210"))
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-210")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayErrorWhenPutRequestIsRejected() {
        // Given
        val task = IntegrationTasks.task("task-1", "Original title", description = "Notes")
        enqueueTasks(task)
        enqueueGetTask(task)
        enqueueUpdateTaskNetworkFailure()
        launchApp()
        openEditForm("task-1")
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput("Updated title")

        // When
        submitEditForm(expectList = false)

        // Then
        assertTitleError("Network request failed")
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
        enqueueTasksForLanguageSwitch(task)
        enqueueGetTask(task)
        launchApp()
        composeTestRule.waitUntilTagExists(TestTags.LANGUAGE_SWITCHER)
        switchToSpanish()
        composeTestRule.waitUntilTagExists(TestTags.taskTitle("task-1"))

        // When
        openEditForm("task-1")

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertTextEquals("Editar tarea")
        assertFieldLabel(TestTags.FIELD_TITLE_LABEL, "Título *")
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().assertTextEquals("Guardar")
    }

    private fun openEditForm(taskId: String) {
        composeTestRule.waitUntilTagExists(TestTags.taskTitle(taskId))
        composeTestRule.onNodeWithTag(TestTags.editButton(taskId)).performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        composeTestRule.waitUntilTagExists(TestTags.EDIT_TASK_TITLE_INPUT)
    }

    private fun submitEditForm(expectList: Boolean = true) {
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick()
        composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        if (expectList) {
            composeTestRule.waitUntilTagExists(TestTags.ADD_TASK_BUTTON)
            composeTestRule.advanceComposeCoroutineIdle(mainDispatcherRule.dispatcher)
        }
    }
}
