package com.example.demo.repository

import com.example.demo.data.remote.TaskApi
import com.example.demo.testing.TaskFixtures
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import com.google.common.truth.Truth.assertThat
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class TaskRepositoryTest {

    private val api = mockk<TaskApi>()
    private val repository = TaskRepository(api)

    @Test
    fun shouldReturnTasksWhenGetTasksSucceeds() = runTest {
        // Given
        val tasks = listOf(TaskFixtures.sampleTask)
        coEvery { api.getTasks() } returns tasks

        // When
        val result = repository.getTasks()

        // Then
        assertThat(result).isEqualTo(tasks)
    }

    @Test
    fun shouldReturnTaskWhenGetTaskSucceeds() = runTest {
        // Given
        coEvery { api.getTask("task-1") } returns TaskFixtures.sampleTask

        // When
        val result = repository.getTask("task-1")

        // Then
        assertThat(result).isEqualTo(TaskFixtures.sampleTask)
    }

    @Test
    fun shouldReturnValidityWhenIsValidSucceeds() = runTest {
        // Given
        coEvery { api.isValid("task-1") } returns true

        // When
        val result = repository.isValid("task-1")

        // Then
        assertThat(result).isTrue()
    }

    @Test
    fun shouldReturnCreatedTaskWhenCreateTaskSucceeds() = runTest {
        // Given
        coEvery { api.createTask(TaskFixtures.sampleRequest) } returns TaskFixtures.sampleTask

        // When
        val result = repository.createTask(TaskFixtures.sampleRequest)

        // Then
        assertThat(result).isEqualTo(TaskFixtures.sampleTask)
    }

    @Test
    fun shouldReturnUpdatedTaskWhenUpdateTaskSucceeds() = runTest {
        // Given
        coEvery { api.updateTask("task-1", TaskFixtures.sampleRequest) } returns TaskFixtures.sampleTask

        // When
        val result = repository.updateTask("task-1", TaskFixtures.sampleRequest)

        // Then
        assertThat(result).isEqualTo(TaskFixtures.sampleTask)
    }

    @Test
    fun shouldCompleteWhenDeleteTaskSucceeds() = runTest {
        // Given
        coEvery { api.deleteTask("task-1") } returns Response.success(Unit)

        // When
        repository.deleteTask("task-1")

        // Then
        coVerify(exactly = 1) { api.deleteTask("task-1") }
    }

    @Test
    fun shouldThrowHttpExceptionWhenDeleteTaskFails() = runTest {
        // Given
        val errorResponse = Response.error<Unit>(
            500,
            "".toResponseBody("application/json".toMediaType())
        )
        coEvery { api.deleteTask("task-1") } returns errorResponse

        // When
        val thrown = runCatching { repository.deleteTask("task-1") }.exceptionOrNull()

        // Then
        assertThat(thrown).isInstanceOf(HttpException::class.java)
        assertThat((thrown as HttpException).code()).isEqualTo(500)
    }
}
