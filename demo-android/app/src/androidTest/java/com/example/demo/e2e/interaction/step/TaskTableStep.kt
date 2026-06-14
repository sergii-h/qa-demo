package com.example.demo.e2e.interaction.step

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performClick
import com.example.demo.MainActivity
import com.example.demo.e2e.interaction.page.CreateTaskForm
import com.example.demo.e2e.interaction.page.EditTaskForm
import com.example.demo.e2e.interaction.page.InfoTaskModal
import com.example.demo.e2e.interaction.page.MainPage
import io.qameta.allure.kotlin.Allure
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.assertIsDisplayed

class TaskTableStep(
    private val rule: AndroidComposeTestRule<*, MainActivity>,
) {
    private val mainPage = MainPage(rule)
    private val createTaskForm = CreateTaskForm(rule)
    private val editTaskForm = EditTaskForm(rule)
    private val infoTaskModal = InfoTaskModal(rule)

    fun openCreateTaskForm(): CreateTaskStep {
        Allure.step("Open 'Create task' form") {
            mainPage.createTaskButton().performClick()
            createTaskForm.titleField().assertIsDisplayed()
        }
        return CreateTaskStep(rule)
    }

    fun openTaskInfoForm(title: String) {
        Allure.step("Open 'Task info' form for task '$title'") {
            mainPage.infoButton(resolveTaskId(title)).performScrollTo().performClick()
            infoTaskModal.waitUntilVisible()
        }
    }

    fun openTaskEditForm(title: String): EditTaskStep {
        Allure.step("Open 'Task edit' form for task '$title'") {
            mainPage.editButton(resolveTaskId(title)).performClick()
            mainPage.waitUntilLoadingSpinnerAbsent()
            editTaskForm.titleField().assertIsDisplayed()
        }
        return EditTaskStep(rule)
    }

    fun deleteTask(title: String) {
        Allure.step("Delete task '$title'") {
            val taskId = resolveTaskId(title)
            mainPage.deleteButton(taskId).performClick()
            mainPage.waitUntilTaskWithTitleAbsent(title)
        }
    }

    private fun resolveTaskId(title: String): String {
        mainPage.waitUntilTaskWithTitlePresent(title)
        val tag = mainPage.taskTitleByTitle(title).fetchSemanticsNode().config[SemanticsProperties.TestTag]
        return tag.removePrefix("task-title-")
    }
}
