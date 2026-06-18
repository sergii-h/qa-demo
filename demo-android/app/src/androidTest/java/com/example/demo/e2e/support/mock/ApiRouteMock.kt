package com.example.demo.e2e.support.mock

import com.example.demo.data.model.Task
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject

class ApiRouteMock(
    private val wireMock: WireMockClient,
) {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val taskAdapter = moshi.adapter(Task::class.java)
    private val tasksAdapter = moshi.adapter<List<Task>>(
        Types.newParameterizedType(List::class.java, Task::class.java),
    )

    fun getTasks(vararg tasks: Task): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "get-tasks",
            request = JSONObject()
                .put("method", "GET")
                .put("urlPath", TASKS_PATH),
            response = jsonResponse(200, tasksAdapter.toJson(tasks.toList())),
        )
        return this
    }

    fun getTask(task: Task): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "get-task",
            request = JSONObject()
                .put("method", "GET")
                .put("urlPathPattern", TASK_BY_ID_PATH),
            response = jsonResponse(200, taskAdapter.toJson(task)),
        )
        return this
    }

    fun getIsValid(isValid: Boolean): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "is-valid",
            request = JSONObject()
                .put("method", "GET")
                .put("urlPathPattern", IS_VALID_PATH),
            response = jsonResponse(200, isValid.toString()),
        )
        return this
    }

    fun createTask(task: Task): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "create-task",
            request = JSONObject()
                .put("method", "POST")
                .put("urlPath", TASKS_PATH),
            response = jsonResponse(200, taskAdapter.toJson(task)),
        )
        return this
    }

    fun updateTask(task: Task): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "update-task",
            request = JSONObject()
                .put("method", "PUT")
                .put("urlPathPattern", TASK_BY_ID_PATH),
            response = jsonResponse(200, taskAdapter.toJson(task)),
        )
        return this
    }

    fun deleteTask(): ApiRouteMock {
        wireMock.addScenarioMapping(
            scenarioName = "delete-task",
            request = JSONObject()
                .put("method", "DELETE")
                .put("urlPathPattern", TASK_BY_ID_PATH),
            response = JSONObject().put("status", 204),
        )
        return this
    }

    private fun jsonResponse(status: Int, body: String): JSONObject = JSONObject()
        .put("status", status)
        .put("body", body)
        .put("headers", JSONObject().put("Content-Type", "application/json"))

    companion object {
        private const val TASKS_PATH = "/v1/tasks"
        private const val TASK_BY_ID_PATH = "/v1/tasks/(?!isValid)[^/]+"
        private const val IS_VALID_PATH = "/v1/tasks/isValid/.+"
    }
}
