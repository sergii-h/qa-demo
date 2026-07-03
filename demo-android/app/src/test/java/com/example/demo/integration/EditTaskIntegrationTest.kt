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
import com.example.demo.integration.support.GetTasksFailure
import com.example.demo.integration.support.GetTaskFailure
import com.example.demo.integration.support.UpdatePutFailure
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class EditTaskIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun openEditForm(taskId: String) {
            runAsyncAction { onNodeWithTag("edit-button-${taskId}").performClick() }
            assertIsDisplayed("edit-task-title-input")
        }

        protected fun setTitle(title: String) {
            runAsyncAction { onNodeWithTag("edit-task-title-input").performTextInput(title) }
        }

        protected fun clearTitle() {
            composeTestRule.onNodeWithTag("edit-task-title-input").performTextClearance()
        }

        protected fun setDescription(description: String) {
            runAsyncAction { onNodeWithTag("task-description-input").performTextInput(description) }
        }

        protected fun clearDescription() {
            composeTestRule.onNodeWithTag("task-description-input").performTextClearance()
        }

        protected fun selectStatus(status: TaskStatus) {
            composeTestRule.onNodeWithTag("status-dropdown").performScrollTo().performClick()
            runAsyncAction { onNodeWithTag("status-dropdown-option-${status.name}").performClick() }
        }

        protected fun selectPriority(priority: TaskPriority) {
            composeTestRule.onNodeWithTag("priority-dropdown").performScrollTo().performClick()
            runAsyncAction { onNodeWithTag("priority-dropdown-option-${priority.name}").performClick() }
        }

        protected fun submitForm() {
            runAsyncAction { onNodeWithTag("save-button").performScrollTo().performClick() }
            assertIsNotDisplayed("edit-task-title-input")
            assertIsNotDisplayed("loading-spinner")
        }

        protected fun clickSubmitForm() {
            runAsyncAction { onNodeWithTag("save-button").performScrollTo().performClick() }
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class EditTaskIntegrationTests : Base() {

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
            assertTextEquals("task-title-${context.id}", updatedContext.title)
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
            assertTextEquals("task-title-${context.id}", updatedContext.title)
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
            runAsyncAction { onNodeWithTag("close-button").performClick() }

            // And
            openEditForm(context.id)

            // Then
            assertThat(mockServer.updateTaskRequests).isEmpty()
            assertTextEquals("edit-task-title-input", context.title)
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

            // When
            clickSubmitForm()

            // Then
            assertTextEquals("title-error", "Title must not exceed 100 characters")
            assertThat(mockServer.updateTaskRequests).isEmpty()

            // When
            clearTitle()
            setTitle(context.title)
            submitForm()

            // Then
            assertThat(mockServer.updateTaskRequests).containsExactly(context.createTaskUpdateRequest())
            assertIsDisplayed("task-title-${context.id}")
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
            assertTextEquals("save-error", "Request failed (500)")
            assertThat(mockServer.updateTaskRequests)
                .containsExactly(updatedContext.createTaskUpdateRequest())

            // When
            submitForm()

            // Then
            assertThat(mockServer.updateTaskRequests).containsExactly(
                updatedContext.createTaskUpdateRequest(),
                updatedContext.createTaskUpdateRequest(),
            )
            assertTextEquals("task-title-${context.id}", updatedContext.title)
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
            assertTextEquals("modal-title", "Editar tarea")
            assertTextEquals("field-title-label", "Título *")
            assertTextEquals("save-button", "Guardar")
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class EditTaskGetFailureIntegrationTest(
        private val failureCase: GetTaskFailure,
    ) : Base() {

        @Test
        fun shouldAllowOpeningEditFormWhenInitialGetTasksFails() {
            // Given
            val context = TaskTestContext()

            mockServer.enqueueGetTasks(context.createTaskResponse())
            failureCase.enqueue(mockServer)
            launchApp()

            assertIsDisplayed("task-title-${context.id}")

            // When
            openEditForm(context.id)

            // Then
            composeTestRule.onNodeWithTag("save-button").assertIsNotEnabled()
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<GetTaskFailure> = GetTaskFailure.entries
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class EditTaskRefreshGetFailureIntegrationTest(
        private val failureCase: GetTasksFailure,
    ) : Base() {

        @Test
        fun shouldCloseEditFormWhenRefreshGetFailsAfterSuccessfulPut() {
            // Given
            val context = TaskTestContext()
            val updatedContext = context.copy(title = context.title + " - Updated title")

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueUpdateTask(updatedContext.createTaskResponse())
            failureCase.enqueue(mockServer)
            launchApp()

            openEditForm(context.id)
            clearTitle()
            setTitle(updatedContext.title)

            // When
            submitForm()

            // Then
            assertIsDisplayed("add-task-button")
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<GetTasksFailure> = GetTasksFailure.entries
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class EditTaskPutFailureIntegrationTest(
        private val failureCase: UpdatePutFailure,
    ) : Base() {

        @Test
        fun shouldDisplayGenericErrorOnEditFormWhenPutRequestFails() {
            // Given
            val context = TaskTestContext()
            val updatedContext = context.copy(title = "Updated title")

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
            failureCase.enqueue(mockServer)
            launchApp()

            openEditForm(context.id)
            clearTitle()
            setTitle(updatedContext.title)

            // When
            clickSubmitForm()

            // Then
            assertTextEquals("save-error", failureCase.expectedSaveError)
            assertThat(mockServer.updateTaskRequests)
                .containsExactly(updatedContext.createTaskUpdateRequest())
            assertIsDisplayed("edit-task-title-input")
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<UpdatePutFailure> = UpdatePutFailure.entries
        }
    }
}
