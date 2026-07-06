package com.example.demo.ui.taskform

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo.R
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.i18n.taskPriorityLabel
import com.example.demo.ui.i18n.taskStatusLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskFormScreen(
    repository: TaskRepository,
    mode: TaskFormMode,
    taskId: String?,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: TaskFormViewModel = viewModel(
        key = "${mode.name}-$taskId",
        factory = TaskFormViewModel.Factory(application, repository, taskId, mode)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.saveSucceeded) {
        if (uiState.saveSucceeded) {
            onSaved()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            when (mode) {
                                TaskFormMode.CREATE -> R.string.new_task
                                TaskFormMode.EDIT -> R.string.edit_task
                            }
                        ),
                        modifier = Modifier.testTag("modal-title"),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("close-button"),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.testTag("loading-spinner"))
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = uiState.title,
                        onValueChange = viewModel::onTitleChange,
                        label = {
                            Text(
                                text = stringResource(R.string.field_title),
                                modifier = Modifier.testTag("field-title-label"),
                            )
                        },
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { error ->
                            {
                                Text(
                                    text = error,
                                    modifier = Modifier.testTag("title-error"),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(
                                when (mode) {
                                    TaskFormMode.CREATE -> "create-task-title-input"
                                    TaskFormMode.EDIT -> "edit-task-title-input"
                                }
                            ),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = uiState.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text(stringResource(R.string.field_description)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("task-description-input"),
                        minLines = 4
                    )

                    EnumDropdown(
                        label = stringResource(R.string.field_status),
                        value = uiState.status,
                        options = TaskStatus.entries,
                        optionLabel = { taskStatusLabel(it) },
                        onSelected = viewModel::onStatusChange,
                        testTag = "status-dropdown",
                        optionTestTag = { status -> "status-dropdown-option-${status.name}" },
                    )

                    EnumDropdown(
                        label = stringResource(R.string.field_priority),
                        value = uiState.priority,
                        options = TaskPriority.entries,
                        optionLabel = { taskPriorityLabel(it) },
                        onSelected = viewModel::onPriorityChange,
                        testTag = "priority-dropdown",
                        optionTestTag = { priority -> "priority-dropdown-option-${priority.name}" },
                    )

                    uiState.saveError?.let { error ->
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.testTag("save-error"),
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = viewModel::save,
                        enabled = uiState.title.trim().isNotEmpty() && !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(
                                when (mode) {
                                    TaskFormMode.CREATE -> "create-button"
                                    TaskFormMode.EDIT -> "save-button"
                                }
                            ),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(20.dp)
                                    .testTag("loading-spinner"),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                stringResource(
                                    when (mode) {
                                        TaskFormMode.CREATE -> R.string.create
                                        TaskFormMode.EDIT -> R.string.save
                                    }
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    value: T,
    options: List<T>,
    optionLabel: @Composable (T) -> String,
    onSelected: (T) -> Unit,
    testTag: String,
    optionTestTag: (T) -> String,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = optionLabel(value),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    modifier = Modifier.testTag(optionTestTag(option)),
                )
            }
        }
    }
}
