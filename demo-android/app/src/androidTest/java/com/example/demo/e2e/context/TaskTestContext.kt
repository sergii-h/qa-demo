package com.example.demo.e2e.context

import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.e2e.data.TaskData
import com.example.demo.e2e.data.TaskResponse
import java.util.UUID

data class TaskTestContext(
    val id: String = UUID.randomUUID().toString(),
    val title: String = randomAlphabetic(12),
    val description: String = randomAlphabetic(12),
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
) {
    fun createTaskData(): TaskData = TaskData(
        title = title,
        description = description,
        status = status,
        priority = priority,
    )

    fun createTaskResponse(): TaskResponse = TaskResponse(
        id = id,
        title = title,
        description = description,
        status = status,
        priority = priority,
    )

    private companion object {
        fun randomAlphabetic(length: Int): String = buildString(length) {
            repeat(length) {
                append(('A'..'Z').random())
            }
        }
    }
}
