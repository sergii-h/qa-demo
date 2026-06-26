package com.example.demo.ui.taskform

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.demo.R
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.model.TaskStatus
import com.example.demo.data.model.TaskValidation
import com.example.demo.locale.AppLocale
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.i18n.isDuplicateTitleError
import com.example.demo.ui.i18n.mapTaskError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class TaskFormMode {
    CREATE,
    EDIT
}

data class TaskFormUiState(
    val mode: TaskFormMode = TaskFormMode.CREATE,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val title: String = "",
    val description: String = "",
    val status: TaskStatus = TaskStatus.TODO,
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val titleError: String? = null,
    val saveError: String? = null,
    val saveSucceeded: Boolean = false
)

class TaskFormViewModel(
    application: Application,
    private val repository: TaskRepository,
    private val taskId: String?,
    private val mode: TaskFormMode
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(TaskFormUiState(mode = mode))
    val uiState: StateFlow<TaskFormUiState> = _uiState.asStateFlow()

    init {
        if (mode == TaskFormMode.EDIT && taskId != null) {
            loadTask(taskId)
        }
    }

    private fun loadTask(taskId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { repository.getTask(taskId) }
                .onSuccess { task ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = task.title,
                            description = task.description.orEmpty(),
                            status = task.status,
                            priority = task.priority
                        )
                    }
                }
                .onFailure {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            title = "",
                            description = "",
                            status = TaskStatus.TODO,
                            priority = TaskPriority.MEDIUM
                        )
                    }
                }
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value, titleError = null) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onStatusChange(value: TaskStatus) {
        _uiState.update { it.copy(status = value) }
    }

    fun onPriorityChange(value: TaskPriority) {
        _uiState.update { it.copy(priority = value) }
    }

    fun save() {
        val state = _uiState.value
        if (state.title.trim().isEmpty()) return
        if (state.title.trim().length > TaskValidation.MAX_TITLE_LENGTH) {
            _uiState.update {
                it.copy(
                    titleError = AppLocale.getString(
                        getApplication(),
                        R.string.error_title_too_long
                    )
                )
            }
            return
        }

        val request = TaskRequest(
            title = state.title.trim(),
            description = state.description.trim().ifEmpty { null },
            status = state.status,
            priority = state.priority
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, titleError = null) }
            val result = when (mode) {
                TaskFormMode.CREATE -> runCatching { repository.createTask(request) }
                TaskFormMode.EDIT -> runCatching {
                    repository.updateTask(requireNotNull(taskId), request)
                }
            }
            result
                .onSuccess {
                    _uiState.update { it.copy(isSaving = false, saveSucceeded = true) }
                }
                .onFailure { error ->
                    val app = getApplication<Application>()
                    if (isDuplicateTitleError(error)) {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                titleError = AppLocale.getString(app, R.string.error_title_already_exists)
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(
                                isSaving = false,
                                saveError = mapTaskError(app, error)
                            )
                        }
                    }
                }
        }
    }

    class Factory(
        private val application: Application,
        private val repository: TaskRepository,
        private val taskId: String?,
        private val mode: TaskFormMode
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TaskFormViewModel(application, repository, taskId, mode) as T
        }
    }
}
