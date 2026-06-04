package com.example.demo.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.ui.i18n.taskPriorityLabel
import com.example.demo.ui.i18n.taskStatusLabel
import com.example.demo.ui.theme.DoneGreen
import com.example.demo.ui.theme.HighRed
import com.example.demo.ui.theme.InProgressOrange
import com.example.demo.ui.theme.LowGreen
import com.example.demo.ui.theme.MediumOrange
import com.example.demo.ui.theme.TodoBlue

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        TaskStatus.TODO -> taskStatusLabel(status) to TodoBlue
        TaskStatus.IN_PROGRESS -> taskStatusLabel(status) to InProgressOrange
        TaskStatus.DONE -> taskStatusLabel(status) to DoneGreen
    }
    TaskChip(label = label, color = color, modifier = modifier)
}

@Composable
fun PriorityChip(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (label, color) = when (priority) {
        TaskPriority.LOW -> taskPriorityLabel(priority) to LowGreen
        TaskPriority.MEDIUM -> taskPriorityLabel(priority) to MediumOrange
        TaskPriority.HIGH -> taskPriorityLabel(priority) to HighRed
    }
    TaskChip(label = label, color = color, modifier = modifier)
}

@Composable
private fun TaskChip(label: String, color: Color, modifier: Modifier = Modifier) {
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label, style = MaterialTheme.typography.labelMedium) },
        modifier = modifier,
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = color.copy(alpha = 0.15f),
            disabledLabelColor = color
        )
    )
}
