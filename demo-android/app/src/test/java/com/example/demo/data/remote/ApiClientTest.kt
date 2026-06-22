package com.example.demo.data.remote

import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

class ErrorBodyParserTest {

    @Test
    fun shouldReturnMessageWhenErrorBodyContainsMessage() {
        // Given
        val body = """{"message":"Title already exists"}"""

        // When
        val result = parseErrorBody(body)

        // Then
        assertThat(result).isEqualTo("Title already exists")
    }
}

@RunWith(Parameterized::class)
class ErrorBodyParserNullResultTest(
    private val scenario: String,
    private val errorBody: String?
) {

    @Test
    fun shouldReturnNullWhenErrorBodyIsInvalid() {
        // When
        val result = parseErrorBody(errorBody)

        // Then
        assertThat(result).isNull()
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun invalidErrorBodies() = listOf(
            arrayOf("no message field", """{}"""),
            arrayOf("message is null", """{"message":null}"""),
            arrayOf("body is null", null),
            arrayOf("body is blank", "   "),
            arrayOf("body is json null", "null")
        )
    }
}
