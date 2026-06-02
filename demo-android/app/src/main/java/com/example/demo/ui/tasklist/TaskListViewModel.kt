package com.example.demo.ui.tasklist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demo.data.model.Task
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.i18n.mapTaskError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val deletingTaskIds: Set<String> = emptySet()
)

class TaskListViewModel(
    application: Application,
    private val repository: TaskRepository
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TaskListUiState())
    val uiState: StateFlow<TaskListUiState> = _uiState.asStateFlow()

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { repository.getTasks() }
                .onSuccess { tasks ->
                    _uiState.update { it.copy(tasks = tasks, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            tasks = emptyList(),
                            isLoading = false,
                            errorMessage = mapTaskError(getApplication(), error)
                        )
                    }
                }
        }
    }

    fun deleteTask(task: Task) {
        viewModelScope.launch {
            _uiState.update { it.copy(deletingTaskIds = it.deletingTaskIds + task.id) }
            runCatching { repository.deleteTask(task.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            tasks = it.tasks.filter { item -> item.id != task.id },
                            deletingTaskIds = it.deletingTaskIds - task.id
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            deletingTaskIds = it.deletingTaskIds - task.id,
                            errorMessage = mapTaskError(getApplication(), error)
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(
        private val application: Application,
        private val repository: TaskRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskListViewModel(application, repository) as T
        }
    }
}
