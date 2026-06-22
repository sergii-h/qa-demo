package com.example.demo.integration

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.integration.support.IntegrationTasks
import com.example.demo.integration.support.IntegrationTestBase
import com.example.demo.integration.support.LanguageOption
import com.example.demo.ui.TestTags
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class TaskDetailIntegrationTest : IntegrationTestBase() {

    @Test
    fun shouldDisplayTaskDetailsWithAllValuesWhenInfoOpened() {
        // Given
        val task = IntegrationTasks.task(
            id = "task-301",
            title = "Info task",
            description = "Info description",
            status = TaskStatus.IN_PROGRESS,
            priority = TaskPriority.HIGH,
        )
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()
        openDetail("task-301")

        // Then
        commonAssertions.assertDescriptionText("Info description")
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.IN_PROGRESS)).assertExists()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.HIGH)).assertExists()
        composeTestRule.onNodeWithTag(TestTags.VALID).assertExists()
    }

    @Test
    fun shouldDisplayTaskDetailsWithRequiredValuesWhenInfoOpened() {
        // Given
        val task = IntegrationTasks.task(
            id = "task-305",
            title = "Info required task",
            description = null,
            status = TaskStatus.TODO,
            priority = TaskPriority.MEDIUM,
        )
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()
        openDetail("task-305")

        // Then
        commonAssertions.assertDescriptionText("No description")
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO)).assertExists()
        composeTestRule.onNodeWithTag(TestTags.priorityTag(TaskPriority.MEDIUM)).assertExists()
        composeTestRule.onNodeWithTag(TestTags.VALID).assertExists()
    }

    @Test
    fun shouldShowNotValidIndicatorWhenValidationReturnsFalse() {
        // Given
        val task = IntegrationTasks.task("task-2", "Invalid Task", description = "Details")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(false)
        launchApp()
        openDetail("task-2")

        // Then
        composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertExists()
        composeTestRule.onNodeWithTag(TestTags.VALID).assertDoesNotExist()
    }

    @Test
    fun shouldCloseDetailFlowWhenBackPressed() {
        // Given
        val task = IntegrationTasks.task("task-1", "Info Task", description = "Info description")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()
        openDetail("task-1")

        // When
        runAsyncAction {
            onNodeWithTag(TestTags.CLOSE_BUTTON).performClick()
        }

        // Then
        composeTestRule.onNodeWithTag(TestTags.taskTitle("task-1")).assertIsDisplayed()
    }

    @Test
    fun shouldKeepDetailFlowAvailableWhenTaskLoadFails() {
        // Given
        val listTask = IntegrationTasks.task("task-1", "Info Task")
        mockServer.enqueueGetTasks(listTask)
        mockServer.enqueueGetTaskError(500)
        mockServer.enqueueIsValid(false)
        launchApp()
        openDetailExpectingLoadError("task-1")

        // Then
        commonAssertions.assertLoadErrorDisplayed()
    }

    @Test
    fun shouldKeepDetailFlowAvailableWhenValidationRequestFails() {
        // Given
        val task = IntegrationTasks.task("task-3", "Validation Task", description = "Validation description")
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValidError(500, "Validation failed")
        launchApp()
        openDetail("task-3")

        // Then
        composeTestRule.onNodeWithTag(TestTags.DESCRIPTION).assertIsDisplayed()
        composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertExists()
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertDoesNotExist()
    }

    @Test
    fun shouldKeepDetailFlowAvailableWhenTaskDetailsRequestIsRejected() {
        // Given
        val listTask = IntegrationTasks.task("task-308", "Task reject task", description = "Task reject description")
        mockServer.enqueueGetTasks(listTask)
        mockServer.enqueueGetTaskNetworkFailure()
        mockServer.enqueueIsValid(false)
        launchApp()
        openDetailExpectingLoadError("task-308")

        // Then
        commonAssertions.assertLoadErrorDisplayed()
    }

    @Test
    fun shouldKeepDetailFlowAvailableWhenValidationRequestIsRejected() {
        // Given
        val task = IntegrationTasks.task(
            id = "task-304",
            title = "Validation reject task",
            description = "Validation reject description",
            status = TaskStatus.DONE,
            priority = TaskPriority.HIGH,
        )
        mockServer.enqueueGetTasks(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValidNetworkFailure()
        launchApp()
        openDetail("task-304")

        // Then
        commonAssertions.assertDescriptionText("Validation reject description")
        composeTestRule.onNodeWithTag(TestTags.NOT_VALID).assertExists()
        composeTestRule.onNodeWithTag(TestTags.LOAD_ERROR).assertDoesNotExist()
    }

    @Test
    fun shouldShowSpanishDetailFlowStringsWhenEsSelected() {
        // Given
        val task = IntegrationTasks.task("task-1", "Info Task", description = "Info description")
        mockServer.enqueueGetTasksForLanguageSwitch(task)
        mockServer.enqueueGetTask(task)
        mockServer.enqueueIsValid(true)
        launchApp()
        switchLanguage(LanguageOption.ES)

        // When
        openDetail("task-1")

        // Then
        commonAssertions.assertFieldLabel(TestTags.DETAIL_DESCRIPTION_LABEL, "Descripción")
        commonAssertions.assertFieldLabel(TestTags.DETAIL_VALIDATED_LABEL, "Validado")
        composeTestRule.onNodeWithTag(TestTags.statusTag(TaskStatus.TODO)).assertTextEquals("Por hacer")
    }

}
