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
import com.example.demo.ui.TestTags
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
                        modifier = Modifier.testTag(TestTags.MODAL_TITLE),
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag(TestTags.CLOSE_BUTTON),
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
                    CircularProgressIndicator(modifier = Modifier.testTag(TestTags.LOADING_SPINNER))
                }
            }
            uiState.loadError != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.loadError ?: stringResource(R.string.failed_load_task),
                        modifier = Modifier.testTag(TestTags.LOAD_ERROR),
                    )
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
                                modifier = Modifier.testTag(TestTags.FIELD_TITLE_LABEL),
                            )
                        },
                        isError = uiState.titleError != null,
                        supportingText = uiState.titleError?.let { error ->
                            {
                                Text(
                                    text = error,
                                    modifier = Modifier.testTag(TestTags.TITLE_ERROR),
                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(
                                when (mode) {
                                    TaskFormMode.CREATE -> TestTags.CREATE_TASK_TITLE_INPUT
                                    TaskFormMode.EDIT -> TestTags.EDIT_TASK_TITLE_INPUT
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
                            .testTag(TestTags.TASK_DESCRIPTION_INPUT),
                        minLines = 4
                    )

                    EnumDropdown(
                        label = stringResource(R.string.field_status),
                        value = uiState.status,
                        options = TaskStatus.entries,
                        optionLabel = { taskStatusLabel(it) },
                        onSelected = viewModel::onStatusChange,
                        testTag = TestTags.STATUS_DROPDOWN,
                        optionTestTag = TestTags::statusDropdownOption,
                    )

                    EnumDropdown(
                        label = stringResource(R.string.field_priority),
                        value = uiState.priority,
                        options = TaskPriority.entries,
                        optionLabel = { taskPriorityLabel(it) },
                        onSelected = viewModel::onPriorityChange,
                        testTag = TestTags.PRIORITY_DROPDOWN,
                        optionTestTag = TestTags::priorityDropdownOption,
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = viewModel::save,
                        enabled = uiState.title.trim().isNotEmpty() && !uiState.isSaving,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(
                                when (mode) {
                                    TaskFormMode.CREATE -> TestTags.CREATE_BUTTON
                                    TaskFormMode.EDIT -> TestTags.SAVE_BUTTON
                                }
                            ),
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .height(20.dp)
                                    .testTag(TestTags.LOADING_SPINNER),
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
