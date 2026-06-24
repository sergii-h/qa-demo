package com.example.demo.repository

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.remote.TaskApi

class TaskRepository(
    private val api: TaskApi
) {
    suspend fun getTasks(): List<Task> = api.getTasks()

    suspend fun getTask(taskId: String): Task = api.getTask(taskId)

    suspend fun isValid(taskId: String): Boolean = api.isValid(taskId)

    suspend fun createTask(request: TaskRequest): Task = api.createTask(request)

    suspend fun updateTask(taskId: String, request: TaskRequest): Task =
        api.updateTask(taskId, request)

    suspend fun deleteTask(taskId: String) {
        api.deleteTask(taskId)
    }

}
