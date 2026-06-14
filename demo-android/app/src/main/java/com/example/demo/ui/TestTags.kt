package com.example.demo.ui

import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus

object TestTags {
    const val PAGE_TITLE = "page-title"
    const val TASK_LIST = "task-list"
    const val REFRESHING = "refreshing"
    const val ADD_TASK_BUTTON = "add-task-button"
    const val EMPTY_TASKS = "empty-tasks"
    const val LOADING_SPINNER = "loading-spinner"
    const val MODAL_TITLE = "modal-title"
    const val CREATE_TASK_TITLE_INPUT = "create-task-title-input"
    const val EDIT_TASK_TITLE_INPUT = "edit-task-title-input"
    const val FIELD_TITLE_LABEL = "field-title-label"
    const val TASK_DESCRIPTION_INPUT = "task-description-input"
    const val STATUS_DROPDOWN = "status-dropdown"
    const val PRIORITY_DROPDOWN = "priority-dropdown"
    const val CREATE_BUTTON = "create-button"
    const val SAVE_BUTTON = "save-button"
    const val CLOSE_BUTTON = "close-button"
    const val TITLE_ERROR = "title-error"
    const val LOAD_ERROR = "load-error"
    const val DESCRIPTION = "description"
    const val DETAIL_DESCRIPTION_LABEL = "detail-description-label"
    const val DETAIL_VALIDATED_LABEL = "detail-validated-label"
    const val CREATED_DATE = "created-date"
    const val UPDATED_DATE = "updated-date"
    const val VALID = "valid"
    const val NOT_VALID = "notValid"
    const val LANGUAGE_SWITCHER = "language-switcher"
    const val LANGUAGE_OPTION_EN = "language-option-en"
    const val LANGUAGE_OPTION_ES = "language-option-es"

    fun taskTitle(taskId: String): String = "task-title-$taskId"

    fun infoButton(taskId: String): String = "info-button-$taskId"

    fun editButton(taskId: String): String = "edit-button-$taskId"

    fun deleteButton(taskId: String): String = "delete-button-$taskId"

    fun statusTag(status: TaskStatus): String = "status-tag-${status.name}"

    fun priorityTag(priority: TaskPriority): String = "priority-tag-${priority.name}"

    fun priorityDropdownOption(priority: TaskPriority): String = "priority-dropdown-option-${priority.name}"

    fun statusDropdownOption(status: TaskStatus): String = "status-dropdown-option-${status.name}"
}
