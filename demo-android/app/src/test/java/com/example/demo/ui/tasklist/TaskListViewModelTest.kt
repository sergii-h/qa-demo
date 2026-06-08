package com.example.demo.ui.tasklist

import android.app.Application
import com.example.demo.R
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class TaskListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = RuntimeEnvironment.getApplication()
    private val repository = mockk<TaskRepository>()

    @Before
    fun setUp() {
        coEvery { repository.getTasks() } returns emptyList()
    }

    @Test
    fun shouldEmitTasksWhenLoadTasksSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val tasks = listOf(TaskFixtures.sampleTask)
        coEvery { repository.getTasks() } returns tasks
        val viewModel = TaskListViewModel(application, repository)

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.tasks).isEqualTo(tasks)
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun shouldEmitErrorWhenLoadTasksFails() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTasks() } throws HttpExceptionFactory.create(500)
        val viewModel = TaskListViewModel(application, repository)

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.tasks).isEmpty()
        assertThat(state.errorMessage)
            .isEqualTo(application.getString(R.string.error_request_failed, 500))
    }

    @Test
    fun shouldRemoveTaskWhenDeleteTaskSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val task = TaskFixtures.sampleTask
        coEvery { repository.getTasks() } returns listOf(task)
        coEvery { repository.deleteTask(task.id) } returns Unit
        val viewModel = TaskListViewModel(application, repository)
        advanceUntilIdle()

        // When
        viewModel.deleteTask(task)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.tasks).isEmpty()
        assertThat(viewModel.uiState.value.deletingTaskIds).isEmpty()
        coVerify { repository.deleteTask(task.id) }
    }

    @Test
    fun shouldEmitErrorWhenDeleteTaskFails() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        val task = TaskFixtures.sampleTask
        coEvery { repository.getTasks() } returns listOf(task)
        coEvery { repository.deleteTask(task.id) } throws HttpExceptionFactory.create(404)
        val viewModel = TaskListViewModel(application, repository)
        advanceUntilIdle()

        // When
        viewModel.deleteTask(task)
        advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.tasks).containsExactly(task)
        assertThat(viewModel.uiState.value.deletingTaskIds).isEmpty()
        assertThat(viewModel.uiState.value.errorMessage)
            .isEqualTo(application.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldClearErrorWhenClearErrorInvoked() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTasks() } throws HttpExceptionFactory.create(500)
        val viewModel = TaskListViewModel(application, repository)
        advanceUntilIdle()
        assertThat(viewModel.uiState.value.errorMessage).isNotNull()

        // When
        viewModel.clearError()

        // Then
        assertThat(viewModel.uiState.value.errorMessage).isNull()
    }
}
