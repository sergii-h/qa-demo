package com.example.demo.data.remote

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface TaskApi {
    @GET("tasks")
    suspend fun getTasks(): List<Task>

    @GET("tasks/{taskId}")
    suspend fun getTask(@Path("taskId") taskId: String): Task

    @GET("tasks/isValid/{taskId}")
    suspend fun isValid(@Path("taskId") taskId: String): Boolean

    @POST("tasks")
    suspend fun createTask(@Body request: TaskRequest): Task

    @PUT("tasks/{taskId}")
    suspend fun updateTask(
        @Path("taskId") taskId: String,
        @Body request: TaskRequest
    ): Task

    @DELETE("tasks/{taskId}")
    suspend fun deleteTask(@Path("taskId") taskId: String): Response<Unit>
}
