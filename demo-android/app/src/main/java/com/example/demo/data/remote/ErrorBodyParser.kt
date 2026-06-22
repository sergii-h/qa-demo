package com.example.demo.data.remote

import com.example.demo.data.model.ErrorResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

private val errorResponseAdapter = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()
    .adapter(ErrorResponse::class.java)

fun parseErrorBody(errorBody: String?): String? {
    if (errorBody.isNullOrBlank()) return null
    return runCatching { errorResponseAdapter.fromJson(errorBody)?.message }.getOrNull()
}
