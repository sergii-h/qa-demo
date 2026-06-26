package com.example.demo.ui.taskdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo.R
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.TestTags
import com.example.demo.ui.components.PriorityChip
import com.example.demo.ui.components.StatusChip
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    repository: TaskRepository,
    taskId: String,
    onBack: () -> Unit
) {
    val application = LocalContext.current.applicationContext as android.app.Application
    val viewModel: TaskDetailViewModel = viewModel(
        key = taskId,
        factory = TaskDetailViewModel.Factory(application, repository, taskId)
    )
    val uiState by viewModel.uiState.collectAsState()
    val locale = LocalConfiguration.current.locales[0]
    val datePattern = stringResource(R.string.date_time_format)
    val dateFormatter = remember(locale, datePattern) {
        DateTimeFormatter.ofPattern(datePattern, locale)
    }
    val notAvailable = stringResource(R.string.not_available)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.task?.title ?: stringResource(R.string.task_info),
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag(TestTags.LOADING_SPINNER),
                    )
                }
                uiState.errorMessage != null -> {
                    Text(
                        text = uiState.errorMessage ?: stringResource(R.string.failed_load_task),
                        modifier = Modifier
                            .align(Alignment.Center)
                            .testTag(TestTags.LOAD_ERROR),
                    )
                }
                uiState.task != null -> {
                    val task = uiState.task!!
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DetailField(
                            label = stringResource(R.string.detail_description),
                            value = task.description?.takeIf { it.isNotBlank() }
                                ?: stringResource(R.string.no_description),
                            labelTestTag = TestTags.DETAIL_DESCRIPTION_LABEL,
                            valueTestTag = TestTags.DESCRIPTION,
                        )
                        DetailField(label = stringResource(R.string.detail_status)) {
                            StatusChip(status = task.status)
                        }
                        DetailField(label = stringResource(R.string.detail_priority)) {
                            PriorityChip(priority = task.priority)
                        }
                        DetailField(
                            label = stringResource(R.string.detail_created),
                            value = formatDate(task.createdDate, dateFormatter, notAvailable),
                            valueTestTag = TestTags.CREATED_DATE,
                        )
                        DetailField(
                            label = stringResource(R.string.detail_last_updated),
                            value = formatDate(task.updatedDate, dateFormatter, notAvailable),
                            valueTestTag = TestTags.UPDATED_DATE,
                        )
                        DetailField(
                            label = stringResource(R.string.detail_validated),
                            labelTestTag = TestTags.DETAIL_VALIDATED_LABEL,
                        ) {
                            if (uiState.isValid) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag(TestTags.VALID),
                                ) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = stringResource(R.string.valid),
                                        tint = MaterialTheme.colorScheme.primary,
                                    )
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .testTag(TestTags.NOT_VALID),
                                ) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = stringResource(R.string.not_valid),
                                        tint = MaterialTheme.colorScheme.error,
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
    value: String,
    valueTestTag: String,
    labelTestTag: String? = null,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = labelTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            modifier = Modifier.testTag(valueTestTag),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun DetailField(
    label: String,
    labelTestTag: String? = null,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            modifier = labelTestTag?.let { Modifier.testTag(it) } ?: Modifier,
        )
        Spacer(modifier = Modifier.height(4.dp))
        content()
    }
}

private fun formatDate(
    value: String?,
    formatter: DateTimeFormatter,
    notAvailable: String
): String {
    if (value.isNullOrBlank()) return notAvailable
    return runCatching {
        val instant = Instant.parse(value)
        formatter.format(instant.atZone(ZoneId.systemDefault()))
    }.getOrDefault(value)
}
