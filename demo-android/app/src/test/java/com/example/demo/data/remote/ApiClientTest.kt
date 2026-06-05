package com.example.demo.data.remote

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class ApiClientTest {

    @Test
    fun shouldReturnMessageWhenErrorBodyContainsMessage() {
        // Given
        val body = """{"message":"Title already exists"}"""

        // When
        val result = ApiClient.parseErrorMessage(body)

        // Then
        assertThat(result).isEqualTo("Title already exists")
    }

    @Test
    fun shouldReturnNullWhenErrorBodyHasNoMessageField() {
        // Given
        val body = """{}"""

        // When
        val result = ApiClient.parseErrorMessage(body)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnNullWhenErrorBodyMessageIsNull() {
        // Given
        val body = """{"message":null}"""

        // When
        val result = ApiClient.parseErrorMessage(body)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnNullWhenErrorBodyIsNull() {
        // When
        val result = ApiClient.parseErrorMessage(null)

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnNullWhenErrorBodyIsBlank() {
        // When
        val result = ApiClient.parseErrorMessage("   ")

        // Then
        assertThat(result).isNull()
    }

    @Test
    fun shouldReturnNullWhenErrorBodyIsJsonNull() {
        // When
        val result = ApiClient.parseErrorMessage("null")

        // Then
        assertThat(result).isNull()
    }
}
