package com.example.demo

import com.example.demo.repository.TaskRepository
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class DemoAppTest {

    @Test
    fun shouldProvideTaskRepositoryWhenApplicationCreated() {
        // Given
        val app = RuntimeEnvironment.getApplication() as DemoApp

        // When
        val repository = app.taskRepository

        // Then
        assertThat(repository).isInstanceOf(TaskRepository::class.java)
    }

    @Test
    fun shouldReturnSameRepositoryInstanceWhenAccessedTwice() {
        // Given
        val app = RuntimeEnvironment.getApplication() as DemoApp

        // When
        val first = app.taskRepository
        val second = app.taskRepository

        // Then
        assertThat(first).isSameInstanceAs(second)
    }
}
