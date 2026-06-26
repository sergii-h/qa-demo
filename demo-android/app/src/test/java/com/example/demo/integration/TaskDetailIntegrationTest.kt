package com.example.demo.integration

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.context.TaskTestContext
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.experimental.runners.Enclosed
import org.robolectric.RobolectricTestRunner

@RunWith(Enclosed::class)
class TaskDetailIntegrationTest {

    abstract class Base : IntegrationTestBase() {

        protected fun openDetail(taskId: String) {
            runAsyncAction { onNodeWithTag(TestTags.infoButton(taskId)).performClick() }
            assertIsDisplayed(TestTags.DESCRIPTION)
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
            assertTextEquals(TestTags.DESCRIPTION, context.description.toString())
            assertIsDisplayed(TestTags.statusTag(context.status))
            assertIsDisplayed(TestTags.priorityTag(context.priority))
            assertIsDisplayed(TestTags.VALID)
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
            assertTextEquals(TestTags.DESCRIPTION, "No description")
            assertIsDisplayed(TestTags.statusTag(context.status))
            assertIsDisplayed(TestTags.priorityTag(context.priority))
            assertIsDisplayed(TestTags.VALID)
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
            runAsyncAction { onNodeWithTag(TestTags.CLOSE_BUTTON).performClick() }

            // Then
            assertIsDisplayed(TestTags.taskTitle(context.id))
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
            assertTextEquals(TestTags.DETAIL_DESCRIPTION_LABEL, "Descripción")
            assertTextEquals(TestTags.DETAIL_VALIDATED_LABEL, "Validado")
            assertTextEquals(TestTags.statusTag(context.status), "Por hacer")
            assertTextEquals(TestTags.priorityTag(context.priority), "Baja")
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
            assertIsDisplayed(TestTags.VALID)
            composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertDoesNotExist()
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
            assertIsDisplayed(TestTags.NOT_VALID)
            composeTestRule.onNodeWithTag(TestTags.VALID).assertDoesNotExist()
        }

        @Test
        fun shouldNotOpenInfoFormWhenTaskDetailsRequestFailsWithHttp500AndDisplayGenericLoadTaskInfoError() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTaskError(500)
                .enqueueIsValid(false)
            launchApp()

            // When
            runAsyncAction { onNodeWithTag(TestTags.infoButton(context.id)).performClick() }

            // Then
            assertTextEquals(TestTags.LOAD_ERROR, "Request failed (500)")
            assertIsNotDisplayed(TestTags.DESCRIPTION)
        }

        @Test
        fun shouldShowInvalidValidationSignWhenValidationRequestFailsWithHttp500AndDisplayGenericLoadTaskInfoError() {
            // Given
            val context = TaskTestContext()

            mockServer
                .enqueueGetTasks(context.createTaskResponse())
                .enqueueGetTask(context.createTaskResponse())
                .enqueueIsValidError(500, "Validation failed")
            launchApp()

            // When
            openDetail(context.id)

            // Then
            assertTextEquals(TestTags.DESCRIPTION, context.description.toString())
            assertIsDisplayed(TestTags.statusTag(context.status))
            assertIsDisplayed(TestTags.priorityTag(context.priority))
            assertIsDisplayed(TestTags.NOT_VALID)
            composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertDoesNotExist()
        }
    }
}
