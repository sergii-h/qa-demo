package com.example.demo.integration.context

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.model.TaskStatus
import java.util.UUID

data class TaskUpdateRequest(val taskId: String, val request: TaskRequest)

data class TaskTestContext(
    var id: String = UUID.randomUUID().toString(),
    var title: String = randomAlphabetic(12),
    var description: String? = randomAlphabetic(12),
    var status: TaskStatus = TaskStatus.TODO,
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var createdDate: String? = "2024-01-15T10:00:00.000Z",
    var updatedDate: String? = "2024-01-16T12:00:00.000Z",
) {
    fun createTaskResponse(): Task = Task(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
        createdDate = createdDate,
        updatedDate = updatedDate,
    )

    fun createTaskRequest(): TaskRequest = TaskRequest(
        title = title,
        description = description,
        status = status,
        priority = priority,
    )

    fun createTaskUpdateRequest(): TaskUpdateRequest = TaskUpdateRequest(
        id,
        createTaskRequest()
    )

    private companion object {
        fun randomAlphabetic(length: Int): String = buildString(length) {
            repeat(length) {
                append(('A'..'Z').random())
            }
        }
    }
}
