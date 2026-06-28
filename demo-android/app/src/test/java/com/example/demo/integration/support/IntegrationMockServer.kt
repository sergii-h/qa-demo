package com.example.demo.integration.support

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import com.example.demo.integration.context.TaskUpdateRequest
import com.example.demo.data.remote.ApiClient
import com.example.demo.data.remote.TaskApi
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import okhttp3.mockwebserver.SocketPolicy
import java.util.ArrayDeque
import java.util.concurrent.TimeUnit

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
    private val _updateTaskRequests = mutableListOf<TaskUpdateRequest>()
    private val _deletedTaskIds = mutableListOf<String>()
    private var _getTasksRequestCount = 0

    val createTaskRequests: List<TaskRequest> get() = _createTaskRequests
    val updateTaskRequests: List<TaskUpdateRequest> get() = _updateTaskRequests
    val deletedTaskIds: List<String> get() = _deletedTaskIds
    val getTasksRequestCount: Int get() = _getTasksRequestCount

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

    fun createTaskApi(): TaskApi {
        val client = OkHttpClient.Builder()
            .retryOnConnectionFailure(false)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        return ApiClient.createTaskApi("${server.url("/")}v1/", client)
    }

    fun enqueueGetTasks(tasks: List<Task>): IntegrationMockServer {
        getTasksScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskListAdapter.toJson(tasks)!!)))
        return this
    }

    fun enqueueGetTasksDelayed(tasks: List<Task>, delayMillis: Long): IntegrationMockServer {
        getTasksScripts.addLast(
            ScriptedResponse.Http(
                jsonResponse(200, taskListAdapter.toJson(tasks)!!)
                    .setBodyDelay(delayMillis, TimeUnit.MILLISECONDS),
            ),
        )
        return this
    }

    fun enqueueGetTasks(vararg tasks: Task): IntegrationMockServer =
        enqueueGetTasks(tasks.toList())

    fun enqueueGetTasksForLanguageSwitch(vararg tasks: Task): IntegrationMockServer {
        val list = tasks.toList()
        return enqueueGetTasks(list).enqueueGetTasks(list)
    }

    fun enqueueGetTask(task: Task): IntegrationMockServer {
        getTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
        return this
    }

    fun enqueueIsValid(isValid: Boolean): IntegrationMockServer {
        isValidScripts.addLast(ScriptedResponse.Http(jsonResponse(200, isValid.toString())))
        return this
    }

    fun enqueueCreateTask(task: Task): IntegrationMockServer {
        createTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
        return this
    }

    fun enqueueUpdateTask(task: Task): IntegrationMockServer {
        updateTaskScripts.addLast(ScriptedResponse.Http(jsonResponse(200, taskAdapter.toJson(task)!!)))
        return this
    }

    fun enqueueGetTasksError(code: Int, message: String? = null): IntegrationMockServer {
        getTasksScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueGetTaskError(code: Int, message: String? = null): IntegrationMockServer {
        getTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueIsValidError(code: Int, message: String? = null): IntegrationMockServer {
        isValidScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueCreateTaskError(code: Int, message: String? = null): IntegrationMockServer {
        createTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueUpdateTaskError(code: Int, message: String? = null): IntegrationMockServer {
        updateTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueDeleteSuccess(): IntegrationMockServer {
        deleteTaskScripts.addLast(ScriptedResponse.Http(MockResponse().setResponseCode(204)))
        return this
    }

    fun enqueueDeleteError(code: Int, message: String? = null): IntegrationMockServer {
        deleteTaskScripts.addLast(ScriptedResponse.Http(errorResponse(code, message)))
        return this
    }

    fun enqueueGetTasksNetworkFailure(): IntegrationMockServer {
        getTasksScripts.addLast(ScriptedResponse.NetworkFailure)
        return this
    }

    fun enqueueGetTaskNetworkFailure(): IntegrationMockServer {
        getTaskScripts.addLast(ScriptedResponse.NetworkFailure)
        return this
    }

    fun enqueueIsValidNetworkFailure(): IntegrationMockServer {
        isValidScripts.addLast(ScriptedResponse.NetworkFailure)
        return this
    }

    fun enqueueCreateTaskNetworkFailure(): IntegrationMockServer {
        createTaskScripts.addLast(ScriptedResponse.NetworkFailure)
        return this
    }

    fun enqueueUpdateTaskNetworkFailure(): IntegrationMockServer {
        updateTaskScripts.addLast(ScriptedResponse.NetworkFailure)
        return this
    }

    fun enqueueDeleteNetworkFailure(): IntegrationMockServer {
        deleteTaskScripts.addLast(
            ScriptedResponse.Http(
                MockResponse()
                    .setResponseCode(503)
                    .setBody("")
                    .addHeader("Content-Type", "application/json")
                    .setSocketPolicy(SocketPolicy.DISCONNECT_DURING_RESPONSE_BODY),
            ),
        )
        return this
    }

    private fun dispatchRequest(request: RecordedRequest): MockResponse {
        val path = request.path ?: error("Missing request path")
        return when {
            request.method == "GET" && path.matches(IS_VALID_PATH) ->
                nextScript(isValidScripts, "GET $path")
            request.method == "GET" && path == TASKS_PATH -> {
                _getTasksRequestCount++
                nextScript(getTasksScripts, "GET $path")
            }
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
                    TaskUpdateRequest(
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
