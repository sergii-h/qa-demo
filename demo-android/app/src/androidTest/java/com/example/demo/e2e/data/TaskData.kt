package com.example.demo.e2e.data

import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

data class TaskData(
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
)
