package com.example.demo.ui.taskdetail

import android.app.Application
import com.example.demo.repository.TaskRepository
import com.example.demo.R
import com.example.demo.testing.HttpExceptionFactory
import com.example.demo.testing.MainDispatcherRule
import com.example.demo.testing.TaskFixtures
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
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
class TaskDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val application: Application = RuntimeEnvironment.getApplication()
    private val repository = mockk<TaskRepository>()
    private val taskId = "task-1"

    @Test
    fun shouldEmitTaskAndValidityWhenLoadSucceeds() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask(taskId) } returns TaskFixtures.sampleTask
        coEvery { repository.isValid(taskId) } returns true
        val viewModel = TaskDetailViewModel(application, repository, taskId)

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.task).isEqualTo(TaskFixtures.sampleTask)
        assertThat(state.isValid).isTrue()
        assertThat(state.errorMessage).isNull()
    }

    @Test
    fun shouldEmitValidityFalseWhenLoadSucceedsAndTaskIsInvalid() =
        runTest(mainDispatcherRule.dispatcher) {
            // Given
            coEvery { repository.getTask(taskId) } returns TaskFixtures.sampleTask
            coEvery { repository.isValid(taskId) } returns false
            val viewModel = TaskDetailViewModel(application, repository, taskId)

            // When
            advanceUntilIdle()

            // Then
            val state = viewModel.uiState.value
            assertThat(state.isLoading).isFalse()
            assertThat(state.task).isEqualTo(TaskFixtures.sampleTask)
            assertThat(state.isValid).isFalse()
        }

    @Test
    fun shouldEmitErrorWhenLoadFails() = runTest(mainDispatcherRule.dispatcher) {
        // Given
        coEvery { repository.getTask(taskId) } throws HttpExceptionFactory.create(404)
        coEvery { repository.isValid(taskId) } returns false
        val viewModel = TaskDetailViewModel(application, repository, taskId)

        // When
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.task).isNull()
        assertThat(state.errorMessage).isEqualTo(application.getString(R.string.error_task_not_found))
    }
}
