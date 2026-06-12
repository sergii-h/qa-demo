package com.example.demo

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
        assertThat(repository).isNotNull()
    }
}
