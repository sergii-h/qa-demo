package com.example.demo.testing

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response

object HttpExceptionFactory {
    fun create(code: Int, body: String = ""): HttpException {
        val response = Response.error<Unit>(
            code,
            body.toResponseBody("application/json".toMediaType())
        )
        return HttpException(response)
    }

}
