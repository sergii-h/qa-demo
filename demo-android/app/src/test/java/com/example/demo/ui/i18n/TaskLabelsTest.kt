package com.example.demo.ui.i18n

import com.example.demo.R
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskStatus
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters

@RunWith(Parameterized::class)
class TaskLabelsTest(
    private val status: TaskStatus,
    private val expectedResId: Int
) {

    @Test
    fun shouldMapStatusToStringResourceWhenStatusProvided() {
        // When
        val resId = taskStatusStringRes(status)

        // Then
        assertThat(resId).isEqualTo(expectedResId)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun statusMappings() = listOf(
            arrayOf(TaskStatus.TODO, R.string.status_todo),
            arrayOf(TaskStatus.IN_PROGRESS, R.string.status_in_progress),
            arrayOf(TaskStatus.DONE, R.string.status_done)
        )
    }
}

@RunWith(Parameterized::class)
class TaskPriorityLabelsTest(
    private val priority: TaskPriority,
    private val expectedResId: Int
) {

    @Test
    fun shouldMapPriorityToStringResourceWhenPriorityProvided() {
        // When
        val resId = taskPriorityStringRes(priority)

        // Then
        assertThat(resId).isEqualTo(expectedResId)
    }

    companion object {
        @JvmStatic
        @Parameters(name = "{0}")
        fun priorityMappings() = listOf(
            arrayOf(TaskPriority.LOW, R.string.priority_low),
            arrayOf(TaskPriority.MEDIUM, R.string.priority_medium),
            arrayOf(TaskPriority.HIGH, R.string.priority_high)
        )
    }
}
