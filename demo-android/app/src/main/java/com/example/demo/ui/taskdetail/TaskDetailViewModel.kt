package com.example.demo.ui.taskdetail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demo.data.model.Task
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.i18n.mapTaskError
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TaskDetailUiState(
    val isLoading: Boolean = true,
    val task: Task? = null,
    val isValid: Boolean = false,
    val errorMessage: String? = null
)

class TaskDetailViewModel(
    application: Application,
    private val repository: TaskRepository,
    private val taskId: String
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TaskDetailUiState())
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                val taskDeferred = async { repository.getTask(taskId) }
                val validDeferred = async { repository.isValid(taskId) }
                taskDeferred.await() to validDeferred.await()
            }.onSuccess { (task, isValid) ->
                _uiState.update {
                    it.copy(isLoading = false, task = task, isValid = isValid)
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        task = null,
                        errorMessage = mapTaskError(getApplication(), error)
                    )
                }
            }
        }
    }

    class Factory(
        private val application: Application,
        private val repository: TaskRepository,
        private val taskId: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskDetailViewModel(application, repository, taskId) as T
        }
    }
}
