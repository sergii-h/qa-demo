package com.example.demo.ui.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demo.data.model.Task
import com.example.demo.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskListUiState(
    val tasks: List<Task> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val taskToDelete: Task? = null,
    val isDeleting: Boolean = false
)

class TaskListViewModel(
    private val repository: TaskRepository
) : ViewModel() {

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
                            errorMessage = repository.mapError(error)
                        )
                    }
                }
        }
    }

    fun requestDelete(task: Task) {
        _uiState.update { it.copy(taskToDelete = task) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(taskToDelete = null) }
    }

    fun confirmDelete() {
        val task = _uiState.value.taskToDelete ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            runCatching { repository.deleteTask(task.id) }
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            tasks = it.tasks.filter { item -> item.id != task.id },
                            taskToDelete = null,
                            isDeleting = false
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            taskToDelete = null,
                            isDeleting = false,
                            errorMessage = repository.mapError(error)
                        )
                    }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    class Factory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskListViewModel(repository) as T
        }
    }
}
