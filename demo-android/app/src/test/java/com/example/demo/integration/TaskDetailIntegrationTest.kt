package com.example.demo.integration

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.context.TaskTestContext
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.integration.support.GetTaskFailure
import com.example.demo.integration.support.IsValidFailure
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.experimental.runners.Enclosed
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class TaskDetailIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun openDetail(taskId: String) {
            runAsyncAction { onNodeWithTag("info-button-${taskId}").performClick() }
            assertIsDisplayed("description")
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class TaskDetailViewIntegrationTests : Base() {

        @Test
        fun shouldOpenInfoFormAndDisplayTaskDetailsForAllValuesDataset() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertTextEquals("description", context.description.toString())
            assertIsDisplayed("status-tag-${context.status.name}")
            assertIsDisplayed("priority-tag-${context.priority.name}")
            assertIsDisplayed("valid")
        }

        @Test
        fun shouldOpenInfoFormAndDisplayTaskDetailsForRequiredOnlyValuesDataset() {
            // Given
            val context = TaskTestContext(description = null)

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertTextEquals("description", "No description")
            assertIsDisplayed("status-tag-${context.status.name}")
            assertIsDisplayed("priority-tag-${context.priority.name}")
            assertIsDisplayed("valid")
        }

        @Test
        fun shouldCloseInfoFormOnCloseAction() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            openDetail(context.id)

            // When
            runAsyncAction { onNodeWithTag("close-button").performClick() }

            // Then
            assertIsDisplayed("task-title-${context.id}")
        }

        @Test
        fun shouldHaveTranslationsForDetailView() {
            // Given
            val context = TaskTestContext(status = TaskStatus.TODO, priority = TaskPriority.LOW)

            mockServer
                .enqueueGetTasksForLanguageSwitch(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            switchLanguage(LanguageOption.ES)

            // When
            openDetail(context.id)

            // Then
            assertTextEquals("detail-description-label", "Descripción")
            assertTextEquals("detail-validated-label", "Validado")
            assertTextEquals("status-tag-${context.status.name}", "Por hacer")
            assertTextEquals("priority-tag-${context.priority.name}", "Baja")
        }
    }

    @RunWith(RobolectricTestRunner::class)
    class ExternalValidationIntegrationTests : Base() {

        @Test
        fun shouldDisplayValidatedStateWhenExternalValidationReturnsTrue() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(true)
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertIsDisplayed("valid")
            composeTestRule.onNodeWithTag("notValid").assertDoesNotExist()
        }

        @Test
        fun shouldDisplayNotValidatedStateWhenExternalValidationReturnsFalse() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValid(false)
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertIsDisplayed("notValid")
            composeTestRule.onNodeWithTag("valid").assertDoesNotExist()
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class TaskDetailLoadFailureIntegrationTests(
        private val failureCase: GetTaskFailure,
    ) : Base() {

        @Test
        fun shouldNotOpenInfoFormWhenTaskDetailsRequestFailsAndDisplayGenericLoadTaskInfoError() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
            failureCase.enqueue(mockServer)
            mockServer.enqueueIsValid(false)
            launchApp()

            // When
            runAsyncAction { onNodeWithTag("info-button-${context.id}").performClick() }

            // Then
            assertTextEquals("load-error", failureCase.expectedLoadError)
            assertIsNotDisplayed("description")
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<GetTaskFailure> = GetTaskFailure.entries
        }
    }

    @RunWith(org.robolectric.ParameterizedRobolectricTestRunner::class)
    class TaskDetailValidationFailureIntegrationTests(
        private val failureCase: IsValidFailure,
    ) : Base() {

        @Test
        fun shouldShowInvalidValidationSignWhenValidationRequestFailsAndDisplayGenericLoadTaskInfoError() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
            failureCase.enqueue(mockServer)
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertTextEquals("description", context.description.toString())
            assertIsDisplayed("status-tag-${context.status.name}")
            assertIsDisplayed("priority-tag-${context.priority.name}")
            assertIsDisplayed("notValid")
            composeTestRule.onNodeWithTag("load-error").assertDoesNotExist()
        }

        companion object {
            @JvmStatic
            @org.robolectric.ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
            fun failureCases(): List<IsValidFailure> = IsValidFailure.entries
        }
    }
}
