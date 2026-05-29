package com.example.demo.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.demo.repository.TaskRepository
import com.example.demo.ui.taskdetail.TaskDetailScreen
import com.example.demo.ui.taskform.TaskFormMode
import com.example.demo.ui.taskform.TaskFormScreen
import com.example.demo.ui.tasklist.TaskListScreen

private object Routes {
    const val TASK_LIST = "tasks"
    const val CREATE_TASK = "create"
    const val EDIT_TASK = "edit/{taskId}"
    const val TASK_DETAIL = "detail/{taskId}"

    fun editTask(taskId: String) = "edit/$taskId"
    fun taskDetail(taskId: String) = "detail/$taskId"
}

@Composable
fun DemoNavHost(repository: TaskRepository) {
    val navController = rememberNavController()
    var listRefreshTrigger by remember { mutableIntStateOf(0) }

    NavHost(
        navController = navController,
        startDestination = Routes.TASK_LIST
    ) {
        composable(Routes.TASK_LIST) {
            TaskListScreen(
                repository = repository,
                refreshTrigger = listRefreshTrigger,
                onCreateTask = { navController.navigate(Routes.CREATE_TASK) },
                onEditTask = { taskId ->
                    navController.navigate(Routes.editTask(taskId))
                },
                onTaskInfo = { taskId ->
                    navController.navigate(Routes.taskDetail(taskId))
                }
            )
        }

        composable(Routes.CREATE_TASK) {
            TaskFormScreen(
                repository = repository,
                mode = TaskFormMode.CREATE,
                taskId = null,
                onBack = { navController.popBackStack() },
                onSaved = {
                    listRefreshTrigger++
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.EDIT_TASK,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { entry ->
            val taskId = entry.arguments?.getString("taskId")
            TaskFormScreen(
                repository = repository,
                mode = TaskFormMode.EDIT,
                taskId = taskId,
                onBack = { navController.popBackStack() },
                onSaved = {
                    listRefreshTrigger++
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Routes.TASK_DETAIL,
            arguments = listOf(navArgument("taskId") { type = NavType.StringType })
        ) { entry ->
            val taskId = entry.arguments?.getString("taskId").orEmpty()
            TaskDetailScreen(
                repository = repository,
                taskId = taskId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
