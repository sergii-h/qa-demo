package com.example.demo.integration

import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.context.TaskTestContext
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class EditTaskIntegrationTests : IntegrationTestBase() {

    @Test
    fun shouldUpdateTaskWithModifiedValuesSendCorrectPutRequestAndShowModifiedTaskTitleInTheListAfterSuccessfulResponse() {
        // Given
        val context = TaskTestContext(status = TaskStatus.TODO, priority = TaskPriority.LOW)

        val updatedContext = context.copy(
            title = context.title + " - Updated title",
            description = context.description + " - Updated description",
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
        )

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks(updatedContext.createTaskResponse())
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle(updatedContext.title)
        clearDescription()
        setDescription(updatedContext.description.toString())
        selectStatus(updatedContext.status)
        selectPriority(updatedContext.priority)

        // When
        submitForm()

        // Then
        assertThat(mockServer.updateTaskRequests)
            .containsExactly(updatedContext.createTaskUpdateRequest())
        assertTextEquals(TestTags.taskTitle(context.id), updatedContext.title)
    }

    @Test
    fun shouldUpdateTaskWithRemovedDescription() {
        // Given
        val context = TaskTestContext()
        val updatedContext = context.copy(
            title = context.title + " - Updated title",
            description = null,
        )

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks(updatedContext.createTaskResponse())
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle(updatedContext.title)
        clearDescription()

        // When
        submitForm()

        // Then
        assertThat(mockServer.updateTaskRequests)
            .containsExactly(updatedContext.createTaskUpdateRequest())
        assertTextEquals(TestTags.taskTitle(context.id), updatedContext.title)
    }

    @Test
    fun shouldNotModifyTaskWhenEditFormIsClosedWithoutSaving() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle("Unsaved title")

        // When
        runAsyncAction { onNodeWithTag(TestTags.CLOSE_BUTTON).performClick() }

        // And
        openEditForm(context.id)

        // Then
        assertThat(mockServer.updateTaskRequests).isEmpty()
        assertTextEquals(TestTags.EDIT_TASK_TITLE_INPUT, context.title)
    }

    @Test
    fun shouldProceedWithSaveAfterUserCorrectsInvalidTitle() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle("a".repeat(101))
        clickSubmitForm()

        assertTextEquals(TestTags.TITLE_ERROR, "Title must not exceed 100 characters")
        assertThat(mockServer.updateTaskRequests).isEmpty()

        // When
        clearTitle()
        setTitle(context.title)
        submitForm()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(context.createTaskUpdateRequest())
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldAllowOpeningEditFormWhenInitialGetTasksFailsWithHttp500() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTaskError(500)
        launchApp()

        // When
        openEditForm(context.id)

        // Then
        assertIsDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
        composeTestRule.onNodeWithTag(TestTags.SAVE_BUTTON).assertIsNotEnabled()
    }

    @Test
    fun shouldCloseEditFormWhenRefreshGetFailsWithHttp500AfterSuccessfulPut() {
        // Given
        val context = TaskTestContext()
        val updatedContext = context.copy(title = context.title + " - Updated title")

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasksError(500)
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle(updatedContext.title)

        // When
        submitForm()

        // Then
        assertIsDisplayed(TestTags.ADD_TASK_BUTTON)
        assertIsNotDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
    }

    @Test
    fun shouldAllowRetryAndSaveTaskAfterInitialPutFailure() {
        // Given
        val context = TaskTestContext()
        val updatedContext = context.copy(title = "Updated title")

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTaskError(500)
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks(updatedContext.createTaskResponse())
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle(updatedContext.title)

        // When
        clickSubmitForm()

        // Then
        assertTextEquals(TestTags.SAVE_ERROR, "Request failed (500)")
        assertThat(mockServer.updateTaskRequests)
            .containsExactly(updatedContext.createTaskUpdateRequest())

        // When
        submitForm()

        // Then
        assertThat(mockServer.updateTaskRequests).containsExactly(
            updatedContext.createTaskUpdateRequest(),
            updatedContext.createTaskUpdateRequest(),
        )
        assertTextEquals(TestTags.taskTitle(context.id), updatedContext.title)
    }

    @Test
    fun shouldDisplayGenericErrorOnEditFormWhenPutRequestIsRejectedWithHttp500() {
        // Given
        val context = TaskTestContext()
        val updatedContext = context.copy(title = "Updated title")

        mockServer
            .enqueueGetTasks(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTaskNetworkFailure()
        launchApp()

        openEditForm(context.id)
        clearTitle()
        setTitle(updatedContext.title)

        // When
        clickSubmitForm()

        // Then
        assertTextEquals(TestTags.SAVE_ERROR, "End of input")
        assertThat(mockServer.updateTaskRequests)
            .containsExactly(updatedContext.createTaskUpdateRequest())
        assertIsDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
    }

    @Test
    fun shouldHaveTranslationsForEditFlow() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasksForLanguageSwitch(context.createTaskResponse())
            .enqueueGetTask(context.createTaskResponse())
        launchApp()

        switchLanguage(LanguageOption.ES)

        // When
        openEditForm(context.id)

        // Then
        assertTextEquals(TestTags.MODAL_TITLE, "Editar tarea")
        assertTextEquals(TestTags.FIELD_TITLE_LABEL, "Título *")
        assertTextEquals(TestTags.SAVE_BUTTON, "Guardar")
    }

    private fun setTitle(title: String) {
        runAsyncAction { onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextInput(title) }
    }

    private fun clearTitle() {
        composeTestRule.onNodeWithTag(TestTags.EDIT_TASK_TITLE_INPUT).performTextClearance()
    }

    private fun setDescription(description: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput(description)
        }
    }

    private fun clearDescription() {
        composeTestRule.onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextClearance()
    }

    private fun submitForm() {
        runAsyncAction { onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick() }
        assertIsNotDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
        assertIsNotDisplayed(TestTags.LOADING_SPINNER)
    }

    private fun clickSubmitForm() {
        runAsyncAction { onNodeWithTag(TestTags.SAVE_BUTTON).performScrollTo().performClick() }
    }

    private fun selectPriority(priority: TaskPriority) {
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).performScrollTo().performClick()
        runAsyncAction { onNodeWithTag(TestTags.priorityDropdownOption(priority)).performClick() }
    }

    private fun selectStatus(status: TaskStatus) {
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).performScrollTo().performClick()
        runAsyncAction { onNodeWithTag(TestTags.statusDropdownOption(status)).performClick() }
    }

    private fun openEditForm(taskId: String) {
        runAsyncAction { onNodeWithTag(TestTags.editButton(taskId)).performClick() }
        assertIsDisplayed(TestTags.EDIT_TASK_TITLE_INPUT)
    }
}
