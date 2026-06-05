package com.example.demo.ui.i18n

import android.app.Application
import com.example.demo.R
import com.example.demo.testing.HttpExceptionFactory
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import retrofit2.Response
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ErrorMessagesTest {

    private val context: Application = RuntimeEnvironment.getApplication()

    @Test
    fun shouldReturnNotFoundMessageWhenHttpNotFound() {
        // Given
        val error = HttpExceptionFactory.create(404)

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldReturnStatusMessageWhenHttpErrorBodyMessageIsBlank() {
        // Given
        val error = HttpExceptionFactory.create(404, """{"message":"   "}""")

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldReturnServerMessageWhenHttpErrorBodyContainsMessage() {
        // Given
        val error = HttpExceptionFactory.create(400, """{"message":"Title is required"}""")

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo("Title is required")
    }

    @Test
    fun shouldReturnDuplicateTitleMessageWhenHttpConflict() {
        // Given
        val error = HttpExceptionFactory.create(409)

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_title_already_exists))
    }

    @Test
    fun shouldIdentifyDuplicateTitleWhenHttpConflict() {
        // Given
        val error = HttpExceptionFactory.create(409)

        // When
        val isDuplicate = isDuplicateTitleError(error)

        // Then
        assertThat(isDuplicate).isTrue()
    }

    @Test
    fun shouldNotIdentifyDuplicateTitleWhenHttpBadRequest() {
        // Given
        val error = HttpExceptionFactory.create(400)

        // When
        val isDuplicate = isDuplicateTitleError(error)

        // Then
        assertThat(isDuplicate).isFalse()
    }

    @Test
    fun shouldReturnInvalidDataMessageWhenHttpBadRequest() {
        // Given
        val error = HttpExceptionFactory.create(400)

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_invalid_task_data))
    }

    @Test
    fun shouldReturnFallbackMessageWhenExceptionHasNoMessage() {
        // Given
        val error = RuntimeException()

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_something_went_wrong))
    }

    @Test
    fun shouldReturnRequestFailedMessageWhenHttpServerError() {
        // Given
        val error = HttpExceptionFactory.create(500)

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_request_failed, 500))
    }

    @Test
    fun shouldReturnStatusMessageWhenHttpResponseIsNull() {
        // Given
        val error = mockk<retrofit2.HttpException> {
            every { code() } returns 404
            every { response() } returns null
        }

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldReturnStatusMessageWhenHttpErrorBodyIsNull() {
        // Given
        val response = mockk<Response<*>> {
            every { errorBody() } returns null
        }
        val error = mockk<retrofit2.HttpException> {
            every { code() } returns 404
            every { response() } returns response
        }

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo(context.getString(R.string.error_task_not_found))
    }

    @Test
    fun shouldNotIdentifyDuplicateTitleWhenNotHttpException() {
        // Given
        val error = IllegalStateException("network down")

        // When
        val isDuplicate = isDuplicateTitleError(error)

        // Then
        assertThat(isDuplicate).isFalse()
    }

    @Test
    fun shouldReturnGenericMessageWhenUnknownException() {
        // Given
        val error = IllegalStateException("network down")

        // When
        val message = mapTaskError(context, error)

        // Then
        assertThat(message).isEqualTo("network down")
    }
}
