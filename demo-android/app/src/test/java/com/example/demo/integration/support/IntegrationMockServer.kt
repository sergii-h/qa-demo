package com.example.demo.integration.support

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.remote.ApiClient
import com.example.demo.data.remote.TaskApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import java.util.ArrayDeque

class IntegrationMockServer {
    private val server = MockWebServer()
    private val getTasksScripts = ArrayDeque<ScriptedResponse>()
    private val getTaskScripts = ArrayDeque<ScriptedResponse>()
    private val isValidScripts = ArrayDeque<ScriptedResponse>()
    private val createTaskScripts = ArrayDeque<ScriptedResponse>()
    private val updateTaskScripts = ArrayDeque<ScriptedResponse>()
    private val deleteTaskScripts = ArrayDeque<ScriptedResponse>()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
    private val taskAdapter = moshi.adapter(Task::class.java)
    private val taskListAdapter = moshi.adapter<List<Task>>(
        Types.newParameterizedType(List::class.java, Task::class.java),
    )
    private val taskRequestAdapter = moshi.adapter(TaskRequest::class.java)

    private val _createTaskRequests = mutableListOf<TaskRequest>()
    private val _updateTaskRequests = mutableListOf<RecordedUpdate>()
    private val _deletedTaskIds = mutableListOf<String>()

    val createTaskRequests: List<TaskRequest> get() = _createTaskRequests
    val updateTaskRequests: List<RecordedUpdate> get() = _updateTaskRequests
    val deletedTaskIds: List<String> get() = _deletedTaskIds

    data class RecordedUpdate(val taskId: String, val request: TaskRequest)

    fun start() {
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse =
                dispatchRequest(request)
        }
        server.start()
    }

    fun shutdown() {
        server.shutdown()
    }

    fun createTaskApi(): TaskApi = ApiClient.createTaskApi("${server.url("/")}v1/")

    fun enqueueGetTasks(tasks: List<Task>) {
        getTasksScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskListAdapter.toJson(tasks)!!)))
    }

    fun enqueueGetTasks(vararg tasks: Task) {
        enqueueGetTasks(tasks.toList())
    }

    fun enqueueGetTasksForLanguageSwitch(vararg tasks: Task) {
        val list = tasks.toList()
        enqueueGetTasks(list)
        enqueueGetTasks(list)
    }

    fun enqueueGetTask(task: Task) {
        getTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
    }

    fun enqueueIsValid(isValid: Boolean) {
        isValidScripts.addLast(ScriptedResponse.Http(jsonResponse(200, isValid.toString())))
    }

    fun enqueueCreateTask(task: Task) {
        createTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
    }

    fun enqueueUpdateTask(task: Task) {
        updateTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
    }

    fun enqueueGetTasksError(code: Int, message: String? = null) {
        getTasksScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueGetTaskError(code: Int, message: String? = null) {
        getTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueIsValidError(code: Int, message: String? = null) {
        isValidScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueCreateTaskError(code: Int, message: String? = null) {
        createTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueUpdateTaskError(code: Int, message: String? = null) {
        updateTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueDeleteSuccess() {
        deleteTaskScripts.addLast(ScriptedResponse.Http(MockResponse().setResponseCode(204)))
    }

    fun enqueueDeleteError(code: Int, message: String? = null) {
        deleteTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
    }

    fun enqueueGetTasksNetworkFailure() {
        getTasksScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    fun enqueueGetTaskNetworkFailure() {
        getTaskScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    fun enqueueIsValidNetworkFailure() {
        isValidScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    fun enqueueCreateTaskNetworkFailure() {
        createTaskScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    fun enqueueUpdateTaskNetworkFailure() {
        updateTaskScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    fun enqueueDeleteNetworkFailure() {
        deleteTaskScripts.addLast(ScriptedResponse.NetworkFailure)
    }

    private fun dispatchRequest(request: RecordedRequest): MockResponse {
        val path = request.path ?: error("Missing request path")
        return when {
            request.method == "GET" && path.matches(IS_VALID_PATH) ->
                nextScript(isValidScripts, "GET $path")
            request.method == "GET" && path == TASKS_PATH ->
                nextScript(getTasksScripts, "GET $path")
            request.method == "GET" && path.matches(TASK_BY_ID_PATH) ->
                nextScript(getTaskScripts, "GET $path")
            request.method == "POST" && path == TASKS_PATH -> {
                _createTaskRequests.add(
                    taskRequestAdapter.fromJson(request.body.readUtf8())!!,
                )
                nextScript(createTaskScripts, "POST $path")
            }
            request.method == "PUT" && path.matches(TASK_BY_ID_PATH) -> {
                val taskId = path.removePrefix("$TASKS_PATH/")
                _updateTaskRequests.add(
                    RecordedUpdate(
                        taskId,
                        taskRequestAdapter.fromJson(request.body.readUtf8())!!,
                    ),
                )
                nextScript(updateTaskScripts, "PUT $path")
            }
            request.method == "DELETE" && path.matches(TASK_BY_ID_PATH) -> {
                _deletedTaskIds.add(path.removePrefix("$TASKS_PATH/"))
                nextScript(deleteTaskScripts, "DELETE $path")
            }
            else -> error("Unexpected request: ${request.method} $path")
        }
    }

    private fun nextScript(scripts: ArrayDeque<ScriptedResponse>, label: String): MockResponse =
        (scripts.poll() ?: error("No scripted response for $label")).toMockResponse()

    private sealed interface ScriptedResponse {
        data class Http(val response: MockResponse) : ScriptedResponse

        data object NetworkFailure : ScriptedResponse {
            override fun toMockResponse(): MockResponse = MockResponse()
                .setSocketPolicy(SocketPolicy.DISCONNECT_AT_START)
        }

        fun toMockResponse(): MockResponse = when (this) {
            is Http -> response
            NetworkFailure -> NetworkFailure.toMockResponse()
        }
    }

    private fun jsonResponse(code: Int, body: String): MockResponse = MockResponse()
        .setResponseCode(code)
        .setBody(body)
        .addHeader("Content-Type", "application/json")

    private fun errorResponse(code: Int, message: String?): MockResponse = MockResponse()
        .setResponseCode(code)
        .setBody(errorJson(message))
        .addHeader("Content-Type", "application/json")

    private fun errorJson(message: String?): String =
        if (message.isNullOrBlank()) {
            ""
        } else {
            """{"message":"$message"}"""
        }

    companion object {
        private const val TASKS_PATH = "/v1/tasks"
        private val TASK_BY_ID_PATH = Regex("/v1/tasks/[^/]+$")
        private val IS_VALID_PATH = Regex("/v1/tasks/isValid/.+")
    }
}
