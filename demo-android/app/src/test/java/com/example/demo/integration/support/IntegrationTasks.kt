package com.example.demo.integration.support

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

object IntegrationTasks {
    fun task(
        id: String,
        title: String,
        description: String? = null,
        status: TaskStatus = TaskStatus.TODO,
        priority: TaskPriority = TaskPriority.MEDIUM,
        createdDate: String? = "2024-01-01T10:00:00Z",
        updatedDate: String? = "2024-01-02T10:00:00Z",
    ) = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        createdDate = createdDate,
        updatedDate = updatedDate,
    )
}