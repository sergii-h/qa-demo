package com.example.demo.ui.i18n

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.demo.R
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

@StringRes
fun taskStatusStringRes(status: TaskStatus): Int = when (status) {
    TaskStatus.TODO -> R.string.status_todo
    TaskStatus.IN_PROGRESS -> R.string.status_in_progress
    TaskStatus.DONE -> R.string.status_done
}

@StringRes
fun taskPriorityStringRes(priority: TaskPriority): Int = when (priority) {
    TaskPriority.LOW -> R.string.priority_low
    TaskPriority.MEDIUM -> R.string.priority_medium
    TaskPriority.HIGH -> R.string.priority_high
}

@Composable
fun taskStatusLabel(status: TaskStatus): String {
    return stringResource(taskStatusStringRes(status))
}

@Composable
fun taskPriorityLabel(priority: TaskPriority): String {
    return stringResource(taskPriorityStringRes(priority))
}
