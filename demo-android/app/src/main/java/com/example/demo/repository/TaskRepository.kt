package com.example.demo.repository

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.remote.ApiClient
import com.example.demo.data.remote.TaskApi
import retrofit2.HttpException

class TaskRepository(
    private val api: TaskApi = ApiClient.taskApi
) {
    suspend fun getTasks(): List<Task> = api.getTasks()

    suspend fun getTask(taskId: String): Task = api.getTask(taskId)

    suspend fun isValid(taskId: String): Boolean = api.isValid(taskId)

    suspend fun createTask(request: TaskRequest): Task = api.createTask(request)

    suspend fun updateTask(taskId: String, request: TaskRequest): Task =
        api.updateTask(taskId, request)

    suspend fun deleteTask(taskId: String) {
        val response = api.deleteTask(taskId)
        if (!response.isSuccessful) {
            throw HttpException(response)
        }
    }

    fun mapError(throwable: Throwable): String {
        if (throwable is HttpException) {
            val message = ApiClient.parseErrorMessage(
                throwable.response()?.errorBody()?.string()
            )
            if (!message.isNullOrBlank()) {
                return message
            }
            return when (throwable.code()) {
                400 -> "Invalid task data"
                404 -> "Task not found"
                409 -> "Task with this title already exists"
                else -> "Request failed (${throwable.code()})"
            }
        }
        return throwable.message ?: "Something went wrong"
    }
}
