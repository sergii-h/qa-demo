package com.example.demo.testing

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.model.TaskStatus

object TaskFixtures {
    val sampleTask = Task(
        id = "task-1",
        title = "Buy milk",
        description = "2%",
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM,
        createdDate = "2024-01-01T10:00:00Z",
        updatedDate = "2024-01-02T10:00:00Z"
    )

    val sampleRequest = TaskRequest(
        title = "Buy milk",
        description = "2%",
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM
    )
}
