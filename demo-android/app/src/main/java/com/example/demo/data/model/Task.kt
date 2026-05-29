package com.example.demo.data.model

data class Task(
    val id: String,
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority,
    val createdDate: String?,
    val updatedDate: String?
)

data class TaskRequest(
    val title: String,
    val description: String?,
    val status: TaskStatus,
    val priority: TaskPriority
)

data class ErrorResponse(
    val message: String?
)
