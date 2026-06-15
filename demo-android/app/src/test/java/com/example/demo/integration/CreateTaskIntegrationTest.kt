package com.example.demo.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
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
class CreateTaskIntegrationTest : IntegrationTestBase() {

    @Test
    fun shouldCreateTaskWithRequiredValues() {
        // Given
        val createdTask = IntegrationTasks.task(
            id = "task-124",
            title = "Test Task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
        )
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTask(createdTask)
        fakeApi.enqueueGetTasks(createdTask)
        launchApp()
        openCreateForm()

        // When
        fillCreateTitle("Test Task")
        submitCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Test Task", null, TaskStatus.TODO, TaskPriority.MEDIUM),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-124")).assertIsDisplayed()
    }

    @Test
    fun shouldCreateTaskWithAllValues() {
        // Given
        val createdTask = IntegrationTasks.task(
            id = "task-123",
            title = "Test Task",
            description = "Test Description",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
        )
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTask(createdTask)
        fakeApi.enqueueGetTasks(createdTask)
        launchApp()
        openCreateForm()

        // When
        fillCreateTitle("Test Task")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Test Description")
        selectStatus(TaskStatus.IN_PROGRESS)
        selectPriority(TaskPriority.HIGH)
        submitCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Test Task", "Test Description", TaskStatus.IN_PROGRESS, TaskPriority.HIGH),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-123")).assertIsDisplayed()
    }

    @Test
    fun shouldResetFormWhenCreateFlowClosedAndReopened() {
        // Given
        fakeApi.enqueueGetTasks()
        launchApp()
        openCreateForm()
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextInput("Temporary title")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Temporary description")

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.CLOSE_BUTTON).performClick()
        }
        openCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).isEmpty()
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertIsDisplayed()
    }

    @Test
    fun shouldProceedWithCreationAfterInvalidTitleCorrected() {
        // Given
        val createdTask = IntegrationTasks.task(
            id = "task-130",
            title = "Corrected title",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
        )
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTask(createdTask)
        fakeApi.enqueueGetTasks(createdTask)
        launchApp()
        openCreateForm()
        fillCreateTitle("a".repeat(101))
        submitCreateForm(expectList = false)
        commonAssertions.assertTitleError("Title must not exceed 100 characters")
        assertThat(fakeApi.createTaskRequests).isEmpty()

        // When
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextClearance()
        fillCreateTitle("Corrected title")
        selectPriority(TaskPriority.HIGH)
        submitCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Corrected title", null, TaskStatus.TODO, TaskPriority.HIGH),
        )
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-130")).assertIsDisplayed()
    }

    @Test
    fun shouldDisplayGenericErrorWhenPostFailsWithServerError() {
        // Given
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTaskError(500)
        launchApp()
        openCreateForm()
        fillCreateTitle("Invalid Task")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Some description")

        // When
        submitCreateForm(expectList = false)

        // Then
        commonAssertions.assertTitleError("Request failed (500)")
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Invalid Task", "Some description", TaskStatus.TODO, TaskPriority.MEDIUM),
        )
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldRetryAndCreateTaskAfterInitialPostFailure() {
        // Given
        val createdTask = IntegrationTasks.task(
            id = "task-456",
            title = "Retry Task",
            description = "Retry Description",
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
        )
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTaskError(500)
        fakeApi.enqueueCreateTask(createdTask)
        fakeApi.enqueueGetTasks(createdTask)
        launchApp()
        openCreateForm()
        fillCreateTitle("Retry Task")
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput("Retry Description")
        selectPriority(TaskPriority.HIGH)

        // When
        val expectedRequest = TaskRequest(
            "Retry Task",
            "Retry Description",
            TaskStatus.TODO,
            TaskPriority.HIGH,
        )
        submitCreateForm(expectList = false)
        commonAssertions.assertTitleError("Request failed (500)")
        assertThat(fakeApi.createTaskRequests).containsExactly(expectedRequest)
        submitCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).containsExactly(expectedRequest, expectedRequest)
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-456")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetFails() {
        // Given
        fakeApi.enqueueGetTasksError(500)

        // When
        launchApp()
        openCreateForm()
        fillCreateTitle("New Task")

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldCloseCreateFlowWhenRefreshGetFailsAfterSuccessfulPost() {
        // Given
        val createdTask = IntegrationTasks.task(
            id = "task-778",
            title = "Task with refresh failure",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.HIGH,
        )
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTask(createdTask)
        fakeApi.enqueueGetTasksError(500)
        launchApp()
        openCreateForm()
        fillCreateTitle("Task with refresh failure")
        selectPriority(TaskPriority.HIGH)

        // When
        submitCreateForm()

        // Then
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Task with refresh failure", null, TaskStatus.TODO, TaskPriority.HIGH),
        )
        composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertDoesNotExist()
    }

    @Test
    fun shouldDisplayErrorWhenPostRequestIsRejected() {
        // Given
        fakeApi.enqueueGetTasks()
        fakeApi.enqueueCreateTaskNetworkFailure()
        launchApp()
        openCreateForm()
        fillCreateTitle("Test Task")
        selectPriority(TaskPriority.HIGH)

        // When
        submitCreateForm(expectList = false)

        // Then
        commonAssertions.assertTitleError("Network request failed")
        assertThat(fakeApi.createTaskRequests).containsExactly(
            TaskRequest("Test Task", null, TaskStatus.TODO, TaskPriority.HIGH),
        )
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performScrollTo().assertExists()
    }

    @Test
    fun shouldKeepCreateFlowAvailableWhenInitialGetRequestIsRejected() {
        // Given
        fakeApi.enqueueGetTasksNetworkFailure()

        // When
        launchApp()
        openCreateForm()

        // Then
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
    }

    @Test
    fun shouldShowSpanishCreateFlowStringsWhenEsSelected() {
        // Given
        fakeApi.enqueueGetTasksForLanguageSwitch()
        launchApp()
        switchLanguage(LanguageOption.ES)

        // When
        openCreateForm()

        // Then
        composeTestRule.onNodeWithTag(TestTags.MODAL_TITLE).assertTextEquals("Nueva tarea")
        commonAssertions.assertFieldLabel(TestTags.FIELD_TITLE_LABEL, "Título *")
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertTextEquals("Crear")
    }

    private fun fillCreateTitle(title: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextInput(title)
        }
        composeTestRule.onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().assertIsEnabled()
    }

    private fun submitCreateForm(expectList: Boolean = true) {
        runAsyncAction {
            onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick()
        }
        if (expectList) {
            composeTestRule.onNodeWithTag(TestTags.ADD_TASK_BUTTON).assertIsDisplayed()
            flushAsyncWork()
        }
    }

    private fun openCreateForm() {
        runAsyncAction {
            onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick()
        }
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).assertIsDisplayed()
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
