package com.example.demo.integration

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
class CreateTaskIntegrationTests : IntegrationTestBase() {

    @Test
    fun shouldCreateTaskWithAllValuesSendCorrectPostRequestAndAddNewTaskToTheListAfterSuccessfulResponse() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        openCreateForm()
        setTitle(context.title)
        setDescription(context.description.toString())
        selectStatus(context.status)
        selectPriority(context.priority)

        // When
        submitCreateForm()

        // Then
        assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldCreateTaskWithRequiredValuesSendCorrectPostRequestAndAddNewTaskToTheListAfterSuccessfulResponse() {
        // Given
        val context = TaskTestContext(description = null, createdDate = null, updatedDate = null)

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        openCreateForm()
        setTitle(context.title)

        // When
        submitCreateForm()

        // Then
        assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldAllowSuccessfulCreationAfterInvalidTitleIsCorrected() {
        // Given
        val context = TaskTestContext(description = null)

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        openCreateForm()
        setTitle("a".repeat(101))

        // When
        clickSubmitForm()

        // Then
        assertTextEquals(TestTags.TITLE_ERROR, "Title must not exceed 100 characters")
        assertThat(mockServer.createTaskRequests).isEmpty()

        // When
        composeTestRule.onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextClearance()
        setTitle(context.title)
        submitCreateForm()

        // Then
        assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldNotCreateTaskWhenCreateFormIsClosedWithoutSavingAndShouldResetFormOnReopen() {
        // Given
        val context = TaskTestContext()

        mockServer.enqueueGetTasks()
        launchApp()

        openCreateForm()
        setTitle(context.title)
        setDescription(context.description.toString())

        // When
        runAsyncAction { onNodeWithTag(TestTags.CLOSE_BUTTON).performClick() }

        // And
        openCreateForm()

        // Then
        assertThat(mockServer.createTaskRequests).isEmpty()
        assertTextEquals(TestTags.CREATE_TASK_TITLE_INPUT, "")
        assertTextEquals(TestTags.TASK_DESCRIPTION_INPUT, "")
    }

    @Test
    fun shouldAllowRetryAndCreateTaskAfterInitialPostFailure() {
        // Given
        val context = TaskTestContext(description = null)

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTaskError(500)
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        openCreateForm()
        setTitle(context.title)

        // When
        clickSubmitForm()

        // Then
        assertTextEquals(TestTags.SAVE_ERROR, "Request failed (500)")
        assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())

        // When
        submitCreateForm()

        // Then
        assertThat(mockServer.createTaskRequests).containsExactly(
            context.createTaskRequest(),
            context.createTaskRequest(),
        )
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldAllowOpeningCreateFormWhenInitialGetTasksFailsWithHttp500() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasksError(500)
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks(context.createTaskResponse())
        launchApp()

        // When
        openCreateForm()
        setTitle(context.title)
        submitCreateForm()

        // Then
        assertIsDisplayed(TestTags.taskTitle(context.id))
    }

    @Test
    fun shouldCloseCreateFormWhenRefreshGetFailsWithHttp500AfterSuccessfulPost() {
        // Given
        val context = TaskTestContext()

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasksError(500)
        launchApp()

        openCreateForm()
        setTitle(context.title)

        // When
        submitCreateForm()

        // Then
        assertIsDisplayed(TestTags.ADD_TASK_BUTTON)
        assertIsNotDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
    }

    @Test
    fun shouldDisplayGenericErrorOnCreateFormWhenPostRequestIsRejectedWithHttp500() {
        // Given
        val context = TaskTestContext(description = null)

        mockServer
            .enqueueGetTasks()
            .enqueueCreateTaskNetworkFailure()
        launchApp()

        openCreateForm()
        setTitle(context.title)

        // When
        clickSubmitForm()

        // Then
        assertTextEquals(TestTags.SAVE_ERROR, "End of input")
        assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
        assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
    }

    @Test
    fun shouldHaveTranslationsForCreateFlow() {
        // Given
        mockServer.enqueueGetTasksForLanguageSwitch()
        launchApp()

        switchLanguage(LanguageOption.ES)

        // When
        openCreateForm()

        // Then
        assertTextEquals(TestTags.MODAL_TITLE, "Nueva tarea")
        assertTextEquals(TestTags.FIELD_TITLE_LABEL, "Título *")
        assertTextEquals(TestTags.CREATE_BUTTON, "Crear")
    }

    private fun setTitle(title: String) {
        runAsyncAction { onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextInput(title) }
    }

    private fun setDescription(description: String) {
        runAsyncAction {
            onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput(description)
        }
    }

    private fun submitCreateForm() {
        runAsyncAction { onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick() }
        assertIsNotDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        assertIsNotDisplayed(TestTags.LOADING_SPINNER)
    }

    private fun clickSubmitForm() {
        runAsyncAction { onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick() }
    }

    private fun selectPriority(priority: TaskPriority) {
        composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).performScrollTo().performClick()
        runAsyncAction { onNodeWithTag(TestTags.priorityDropdownOption(priority)).performClick() }
    }

    private fun selectStatus(status: TaskStatus) {
        composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).performScrollTo().performClick()
        runAsyncAction { onNodeWithTag(TestTags.statusDropdownOption(status)).performClick() }
    }

    private fun openCreateForm() {
        runAsyncAction { onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick() }
        assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
    }
}
