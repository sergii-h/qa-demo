package com.example.demo.ui.tasklist

import android.app.Application
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.demo.R
import com.example.demo.data.model.Task
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.TestTags
import com.example.demo.ui.components.LanguageSwitcher
import com.example.demo.ui.components.PriorityChip
import com.example.demo.ui.components.StatusChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskListScreen(
    repository: TaskRepository,
    onCreateTask: () -> Unit,
    onEditTask: (String) -> Unit,
    onTaskInfo: (String) -> Unit,
    refreshTrigger: Int = 0
) {
    val application = LocalContext.current.applicationContext as Application
    val viewModel: TaskListViewModel = viewModel(
        factory = TaskListViewModel.Factory(application, repository)
    )
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(refreshTrigger) {
        if (refreshTrigger > 0) {
            viewModel.refreshTasks()
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) {
            pullToRefreshState.animateToHidden()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.tasks_title),
                        modifier = Modifier.testTag(TestTags.PAGE_TITLE),
                    )
                },
                actions = {
                    LanguageSwitcher(modifier = Modifier.padding(end = 4.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateTask,
                modifier = Modifier.testTag(TestTags.ADD_TASK_BUTTON),
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_task)
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        PullToRefreshBox(
            state = pullToRefreshState,
            isRefreshing = uiState.isRefreshing,
            onRefresh = viewModel::refreshTasks,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = uiState.isRefreshing,
                    state = pullToRefreshState,
                )
            },
        ) {
            if (uiState.isRefreshing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .testTag(TestTags.REFRESHING),
                )
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag(TestTags.TASK_LIST),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                when {
                    uiState.isLoading -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .padding(vertical = 48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.testTag(TestTags.LOADING_SPINNER),
                                )
                            }
                        }
                    }
                    uiState.tasks.isEmpty() -> {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillParentMaxSize()
                                    .testTag(TestTags.EMPTY_TASKS),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(
                                    text = stringResource(R.string.empty_tasks),
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                    }
                    else -> {
                        items(uiState.tasks, key = { it.id }) { task ->
                            TaskRow(
                                task = task,
                                onInfo = { onTaskInfo(task.id) },
                                onEdit = { onEditTask(task.id) },
                                onDelete = { viewModel.deleteTask(task) },
                                isDeleting = task.id in uiState.deletingTaskIds,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskRow(
    task: Task,
    onInfo: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    isDeleting: Boolean
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = task.title,
                modifier = Modifier.testTag(TestTags.taskTitle(task.id)),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusChip(status = task.status)
                PriorityChip(priority = task.priority)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onInfo,
                    modifier = Modifier.testTag(TestTags.infoButton(task.id)),
                ) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = stringResource(R.string.action_info)
                    )
                }
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.testTag(TestTags.editButton(task.id)),
                ) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = stringResource(R.string.action_edit)
                    )
                }
                IconButton(
                    onClick = onDelete,
                    enabled = !isDeleting,
                    modifier = Modifier.testTag(TestTags.deleteButton(task.id)),
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = stringResource(R.string.action_delete)
                    )
                }
            }
        }
    }
}
