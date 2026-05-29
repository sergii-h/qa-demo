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
import com.example.demo.ui.theme.DoneGreen
import com.example.demo.ui.theme.HighRed
import com.example.demo.ui.theme.InProgressOrange
import com.example.demo.ui.theme.LowGreen
import com.example.demo.ui.theme.MediumOrange
import com.example.demo.ui.theme.TodoBlue

@Composable
fun StatusChip(status: TaskStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        TaskStatus.TODO -> "To Do" to TodoBlue
        TaskStatus.IN_PROGRESS -> "In Progress" to InProgressOrange
        TaskStatus.DONE -> "Done" to DoneGreen
    }
    TaskChip(label = label, color = color, modifier = modifier)
}

@Composable
fun PriorityChip(priority: TaskPriority, modifier: Modifier = Modifier) {
    val (label, color) = when (priority) {
        TaskPriority.LOW -> "Low" to LowGreen
        TaskPriority.MEDIUM -> "Medium" to MediumOrange
        TaskPriority.HIGH -> "High" to HighRed
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
