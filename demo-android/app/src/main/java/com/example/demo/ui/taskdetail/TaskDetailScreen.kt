package com.example.demo.ui.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.components.PriorityChip
import com.example.demo.ui.components.StatusChip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val dateFormatter = DateTimeFormatter.ofPattern(
    "MMM d, yyyy HH:mm",
    Locale.getDefault()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    repository: TaskRepository,
    taskId: String,
    onBack: () -> Unit
) {
    val viewModel: TaskDetailViewModel = viewModel(
        key = taskId,
        factory = TaskDetailViewModel.Factory(repository, taskId)
    )
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.task?.title ?: "Task info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: "Failed to load task",
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.task != null -> {
                    val task = uiState.task!!
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailField(
                            label = "Description",
                            value = task.description?.takeIf { it.isNotBlank() } ?: "No description"
                        )
                        DetailField(label = "Status") {
                            StatusChip(status = task.status)
                        }
                        DetailField(label = "Priority") {
                            PriorityChip(priority = task.priority)
                        }
                        DetailField(
                            label = "Created",
                            value = formatDate(task.createdDate)
                        )
                        DetailField(
                            label = "Last updated",
                            value = formatDate(task.updatedDate)
                        )
                        DetailField(label = "Validated") {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (uiState.isValid) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Valid",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                } else {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Not valid",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailField(
    label: String,
    value: String
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun DetailField(
    label: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

private fun formatDate(value: String?): String {
    if (value.isNullOrBlank()) return "N/A"
    return runCatching {
        val instant = Instant.parse(value)
        dateFormatter.format(instant.atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}
