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

}
