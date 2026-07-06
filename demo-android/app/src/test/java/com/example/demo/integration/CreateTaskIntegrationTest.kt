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
import com.example.demo.integration.support.CreatePostFailure
import com.example.demo.integration.support.GetTasksFailure
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class CreateTaskIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun openCreateForm() {
            runAsyncAction { onNodeWithTag("add-task-button").performClick() }
            assertIsDisplayed("create-task-title-input")
        }

        protected fun setTitle(title: String) {
            runAsyncAction { onNodeWithTag("create-task-title-input").performTextInput(title) }
        }

        protected fun setDescription(description: String) {
            runAsyncAction { onNodeWithTag("task-description-input").performTextInput(description) }
        }

        protected fun selectStatus(status: TaskStatus) {
            composeTestRule.onNodeWithTag("status-dropdown").performScrollTo().performClick()
            runAsyncAction { onNodeWithTag("status-dropdown-option-${status.name}").performClick() }
        }

        protected fun selectPriority(priority: TaskPriority) {
            composeTestRule.onNodeWithTag("priority-dropdown").performScrollTo().performClick()
            runAsyncAction { onNodeWithTag("priority-dropdown-option-${priority.name}").performClick() }
        }

        protected fun submitCreateForm() {
            runAsyncAction { onNodeWithTag("create-button").performScrollTo().performClick() }
            assertIsNotDisplayed("create-task-title-input")
            assertIsNotDisplayed("loading-spinner")
        }

        protected fun clickSubmitForm() {
            runAsyncAction { onNodeWithTag("create-button").performScrollTo().performClick() }
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class CreateTaskIntegrationTests : Base() {

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
            assertIsDisplayed("task-title-${context.id}")
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
            assertIsDisplayed("task-title-${context.id}")
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
            assertTextEquals("title-error", "Title must not exceed 100 characters")
            assertThat(mockServer.createTaskRequests).isEmpty()

            // When
            composeTestRule.onNodeWithTag("create-task-title-input").performTextClearance()
            setTitle(context.title)
            submitCreateForm()

            // Then
            assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
            assertIsDisplayed("task-title-${context.id}")
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
            runAsyncAction { onNodeWithTag("close-button").performClick() }

            // And
            openCreateForm()

            // Then
            assertThat(mockServer.createTaskRequests).isEmpty()
            assertTextEquals("create-task-title-input", "")
            assertTextEquals("task-description-input", "")
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
            assertTextEquals("save-error", "Request failed (500)")
            assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())

            // When
            submitCreateForm()

            // Then
            assertThat(mockServer.createTaskRequests).containsExactly(
                context.createTaskRequest(),
                context.createTaskRequest(),
            )
            assertIsDisplayed("task-title-${context.id}")
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
            assertTextEquals("modal-title", "Nueva tarea")
            assertTextEquals("field-title-label", "Título *")
            assertTextEquals("create-button", "Crear")
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class CreateTaskInitialGetFailureIntegrationTest(
        private val failureCase: GetTasksFailure,
    ) : Base() {

        @Test
        fun shouldAllowOpeningCreateFormWhenInitialGetTasksFails() {
            // Given
            val context = TaskTestContext()

            failureCase.enqueue(mockServer)
            mockServer
                .enqueueCreateTask(context.createTaskResponse())
                .enqueueGetTasks(context.createTaskResponse())
            launchApp()

            // When
            openCreateForm()
            setTitle(context.title)
            submitCreateForm()

            // Then
            assertIsDisplayed("task-title-${context.id}")
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<GetTasksFailure> = GetTasksFailure.entries
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class CreateTaskRefreshGetFailureIntegrationTest(
        private val failureCase: GetTasksFailure,
    ) : Base() {

        @Test
        fun shouldCloseCreateFormWhenRefreshGetFailsAfterSuccessfulPost() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks()
                .enqueueCreateTask(context.createTaskResponse())
            failureCase.enqueue(mockServer)
            launchApp()

            openCreateForm()
            setTitle(context.title)

            // When
            submitCreateForm()

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
    class CreateTaskPostFailureIntegrationTest(
        private val failureCase: CreatePostFailure,
    ) : Base() {

        @Test
        fun shouldDisplayGenericErrorOnCreateFormWhenPostRequestFails() {
            // Given
            val context = TaskTestContext(description = null)

            mockServer.enqueueGetTasks()
            failureCase.enqueue(mockServer)
            launchApp()

            openCreateForm()
            setTitle(context.title)

            // When
            clickSubmitForm()

            // Then
            assertTextEquals("save-error", failureCase.expectedSaveError)
            assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
            assertIsDisplayed("create-task-title-input")
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<CreatePostFailure> = CreatePostFailure.entries
        }
    }
}
