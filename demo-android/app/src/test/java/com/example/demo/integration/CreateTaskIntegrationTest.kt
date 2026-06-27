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
import com.example.demo.ui.TestTags
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class CreateTaskIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun openCreateForm() {
            runAsyncAction { onNodeWithTag(TestTags.ADD_TASK_BUTTON).performClick() }
            assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        }

        protected fun setTitle(title: String) {
            runAsyncAction { onNodeWithTag(TestTags.CREATE_TASK_TITLE_INPUT).performTextInput(title) }
        }

        protected fun setDescription(description: String) {
            runAsyncAction { onNodeWithTag(TestTags.TASK_DESCRIPTION_INPUT).performTextInput(description) }
        }

        protected fun selectStatus(status: TaskStatus) {
            composeTestRule.onNodeWithTag(TestTags.STATUS_DROPDOWN).performScrollTo().performClick()
            runAsyncAction { onNodeWithTag(TestTags.statusDropdownOption(status)).performClick() }
        }

        protected fun selectPriority(priority: TaskPriority) {
            composeTestRule.onNodeWithTag(TestTags.PRIORITY_DROPDOWN).performScrollTo().performClick()
            runAsyncAction { onNodeWithTag(TestTags.priorityDropdownOption(priority)).performClick() }
        }

        protected fun submitCreateForm() {
            runAsyncAction { onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick() }
            assertIsNotDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
            assertIsNotDisplayed(TestTags.LOADING_SPINNER)
        }

        protected fun clickSubmitForm() {
            runAsyncAction { onNodeWithTag(TestTags.CREATE_BUTTON).performScrollTo().performClick() }
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
            assertIsDisplayed(TestTags.taskTitle(context.id))
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
            assertIsDisplayed(TestTags.ADD_TASK_BUTTON)
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
            assertTextEquals(TestTags.SAVE_ERROR, failureCase.expectedSaveError)
            assertThat(mockServer.createTaskRequests).containsExactly(context.createTaskRequest())
            assertIsDisplayed(TestTags.CREATE_TASK_TITLE_INPUT)
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<CreatePostFailure> = CreatePostFailure.entries
        }
    }
}
