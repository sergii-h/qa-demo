package com.example.demo.e2e.data

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

data class TaskResponse(
    val id: String,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
) {
    fun toTask(): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        createdDate = MOCK_CREATED_DATE,
        updatedDate = MOCK_UPDATED_DATE,
    )

    private companion object {
        const val MOCK_CREATED_DATE = "2024-01-01T10:00:00Z"
        const val MOCK_UPDATED_DATE = "2024-01-02T10:00:00Z"
    }
}
