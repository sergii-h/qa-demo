package com.example.demo.ui.taskform

import android.app.Application
import com.example.demo.R
import com.example.demo.data.model.TaskRequest
import com.example.demo.repository.TaskRepository
import com.example.demo.testing.HttpExceptionFactory
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TaskFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = RuntimeEnvironment.getApplication()
    private val repository = mockk<TaskRepository>()

    @Test
    fun shouldClearDescriptionWhenEditModeTaskHasNoDescription() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            coEvery { repository.getTask("task-1") } returns
                TaskFixtures.sampleTask.copy(description = null)
            val viewModel = TaskFormViewModel(
                application,
                repository,
                taskId = "task-1",
                mode = TaskFormMode.EDIT
            )

            // When
            advanceUntilIdle()

            // Then
            assertThat(viewModel.uiState.value.description).isEmpty()
        }

    @Test
    fun shouldPopulateFieldsWhenEditModeLoadSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.title).isEqualTo(TaskFixtures.sampleTask.title)
        assertThat(state.description).isEqualTo(TaskFixtures.sampleTask.description)
        assertThat(state.status).isEqualTo(TaskFixtures.sampleTask.status)
        assertThat(state.priority).isEqualTo(TaskFixtures.sampleTask.priority)
    }

    @Test
    fun shouldSetTitleErrorWhenTitleExceedsMaxLength() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("a".repeat(101))

        // When
        viewModel.save()

        // Then
        assertThat(viewModel.uiState.value.titleError).isEqualTo(
            application.getString(R.string.error_title_too_long)
        )
        assertThat(viewModel.uiState.value.isSaving).isFalse()
    }

    @Test
    fun shouldSetSaveSucceededWhenCreateSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.createTask(any()) } returns TaskFixtures.sampleTask
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("New task")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.saveSucceeded).isTrue()
        assertThat(viewModel.uiState.value.isSaving).isFalse()
        coVerify {
            repository.createTask(
                TaskRequest(
                    title = "New task",
                    description = null,
                    status = TaskFixtures.sampleRequest.status,
                    priority = TaskFixtures.sampleRequest.priority
                )
            )
        }
    }

    @Test
    fun shouldSendNullDescriptionWhenDescriptionIsBlank() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.createTask(any()) } returns TaskFixtures.sampleTask
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("New task")
        viewModel.onDescriptionChange("   ")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        coVerify {
            repository.createTask(
                TaskRequest(
                    title = "New task",
                    description = null,
                    status = TaskFixtures.sampleRequest.status,
                    priority = TaskFixtures.sampleRequest.priority
                )
            )
        }
    }

    @Test
    fun shouldCreateViewModelWhenFactoryCreateInvoked() {
        // Given
        val factory = TaskFormViewModel.Factory(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )

        // When
        val viewModel = factory.create(TaskFormViewModel::class.java)

        // Then
        assertThat(viewModel.uiState.value.mode).isEqualTo(TaskFormMode.CREATE)
    }

    @Test
    fun shouldSetDuplicateTitleErrorWhenCreateReturnsConflict() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.createTask(any()) } throws HttpExceptionFactory.create(409)
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("Duplicate")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.titleError).isEqualTo(
            application.getString(R.string.error_title_already_exists)
        )
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
    }

    @Test
    fun shouldEmitLoadErrorWhenEditModeLoadFails() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } throws HttpExceptionFactory.create(404)
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )

        // When
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.loadError)
            .isEqualTo(application.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldSetSaveSucceededWhenUpdateSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.updateTask("task-1", any()) } returns TaskFixtures.sampleTask
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )
        advanceUntilIdle()
        viewModel.onTitleChange("Updated title")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.saveSucceeded).isTrue()
        coVerify { repository.updateTask("task-1", any()) }
    }

    @Test
    fun shouldSetSaveErrorWhenSaveFailsWithNonConflictError() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.createTask(any()) } throws HttpExceptionFactory.create(500)
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("New task")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.saveError)
            .isEqualTo(application.getString(R.string.error_request_failed, 500))
        assertThat(viewModel.uiState.value.titleError).isNull()
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
    }

    @Test
    fun shouldUpdateDescriptionStatusAndPriorityWhenHandlersInvoked() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val viewModel = TaskFormViewModel(
                application,
                repository,
                taskId = null,
                mode = TaskFormMode.CREATE
            )

            // When
            viewModel.onDescriptionChange("Notes")
            viewModel.onStatusChange(com.example.demo.data.model.TaskStatus.DONE)
            viewModel.onPriorityChange(com.example.demo.data.model.TaskPriority.HIGH)

            // Then
            val state = viewModel.uiState.value
            assertThat(state.description).isEqualTo("Notes")
            assertThat(state.status).isEqualTo(com.example.demo.data.model.TaskStatus.DONE)
            assertThat(state.priority).isEqualTo(com.example.demo.data.model.TaskPriority.HIGH)
        }

    @Test
    fun shouldNotLoadTaskWhenEditModeHasNoTaskId() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.EDIT
        )

        // When
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        assertThat(viewModel.uiState.value.title).isEmpty()
        coVerify(exactly = 0) { repository.getTask(any()) }
    }

    @Test
    fun shouldSetDuplicateTitleErrorWhenUpdateReturnsConflict() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.updateTask("task-1", any()) } throws HttpExceptionFactory.create(409)
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )
        advanceUntilIdle()
        viewModel.onTitleChange("Duplicate")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.titleError).isEqualTo(
            application.getString(R.string.error_title_already_exists)
        )
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
    }

    @Test
    fun shouldSetSaveErrorWhenUpdateFailsWithNonConflictError() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        coEvery { repository.updateTask("task-1", any()) } throws HttpExceptionFactory.create(500)
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )
        advanceUntilIdle()
        viewModel.onTitleChange("Updated title")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.saveError)
            .isEqualTo(application.getString(R.string.error_request_failed, 500))
        assertThat(viewModel.uiState.value.titleError).isNull()
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
    }

    @Test
    fun shouldSetSaveErrorWhenSaveInvokedInEditModeWithoutTaskId() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            val viewModel = TaskFormViewModel(
                application,
                repository,
                taskId = null,
                mode = TaskFormMode.EDIT
            )
            viewModel.onTitleChange("Title")

            // When
            viewModel.save()
            advanceUntilIdle()

            // Then
            assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
            assertThat(viewModel.uiState.value.saveError).isNotNull()
            assertThat(viewModel.uiState.value.titleError).isNull()
            coVerify(exactly = 0) { repository.updateTask(any(), any()) }
        }

    @Test
    fun shouldClearTitleErrorWhenTitleChanged() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("a".repeat(101))
        viewModel.save()
        assertThat(viewModel.uiState.value.titleError).isNotNull()

        // When
        viewModel.onTitleChange("Valid title")

        // Then
        assertThat(viewModel.uiState.value.titleError).isNull()
    }

    @Test
    fun shouldNotCallRepositoryWhenTitleIsBlankInEditMode() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask("task-1") } returns TaskFixtures.sampleTask
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = "task-1",
            mode = TaskFormMode.EDIT
        )
        advanceUntilIdle()
        viewModel.onTitleChange("   ")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.titleError).isNull()
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
        coVerify(exactly = 0) { repository.updateTask(any(), any()) }
    }

    @Test
    fun shouldNotCallRepositoryWhenTitleIsBlank() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val viewModel = TaskFormViewModel(
            application,
            repository,
            taskId = null,
            mode = TaskFormMode.CREATE
        )
        viewModel.onTitleChange("   ")

        // When
        viewModel.save()
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.titleError).isNull()
        assertThat(viewModel.uiState.value.saveSucceeded).isFalse()
        coVerify(exactly = 0) { repository.createTask(any()) }
    }
}
