package com.example.demo.integration

import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.remote.TaskApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.util.ArrayDeque

class FakeTaskApi : TaskApi {
    private val getTasksScripts = ArrayDeque<ScriptedCall>()
    private val getTaskScripts = ArrayDeque<ScriptedCall>()
    private val isValidScripts = ArrayDeque<ScriptedCall>()
    private val createTaskScripts = ArrayDeque<ScriptedCall>()
    private val updateTaskScripts = ArrayDeque<ScriptedCall>()
    private val deleteTaskScripts = ArrayDeque<ScriptedCall>()

    val deletedTaskIds = mutableListOf<String>()
    val createTaskRequests = mutableListOf<TaskRequest>()
    val updateTaskRequests = mutableListOf<RecordedUpdate>()

    data class RecordedUpdate(val taskId: String, val request: TaskRequest)

    fun enqueueGetTasks(tasks: List<Task>) {
        getTasksScripts.addLast(ScriptedCall.Success(tasks))
    }

    fun enqueueGetTask(task: Task) {
        getTaskScripts.addLast(ScriptedCall.Success(task))
    }

    fun enqueueIsValid(isValid: Boolean) {
        isValidScripts.addLast(ScriptedCall.Success(isValid))
    }

    fun enqueueCreateTask(task: Task) {
        createTaskScripts.addLast(ScriptedCall.Success(task))
    }

    fun enqueueUpdateTask(task: Task) {
        updateTaskScripts.addLast(ScriptedCall.Success(task))
    }

    fun enqueueGetTasksError(code: Int, message: String? = null) {
        getTasksScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueGetTaskError(code: Int, message: String? = null) {
        getTaskScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueIsValidError(code: Int, message: String? = null) {
        isValidScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueCreateTaskError(code: Int, message: String? = null) {
        createTaskScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueUpdateTaskError(code: Int, message: String? = null) {
        updateTaskScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueDeleteSuccess() {
        deleteTaskScripts.addLast(ScriptedCall.EmptySuccess)
    }

    fun enqueueDeleteError(code: Int, message: String? = null) {
        deleteTaskScripts.addLast(ScriptedCall.Error(code, message))
    }

    fun enqueueGetTasksNetworkFailure() {
        getTasksScripts.addLast(ScriptedCall.NetworkFailure)
    }

    fun enqueueGetTaskNetworkFailure() {
        getTaskScripts.addLast(ScriptedCall.NetworkFailure)
    }

    fun enqueueIsValidNetworkFailure() {
        isValidScripts.addLast(ScriptedCall.NetworkFailure)
    }

    fun enqueueCreateTaskNetworkFailure() {
        createTaskScripts.addLast(ScriptedCall.NetworkFailure)
    }

    fun enqueueUpdateTaskNetworkFailure() {
        updateTaskScripts.addLast(ScriptedCall.NetworkFailure)
    }

    fun enqueueDeleteNetworkFailure() {
        deleteTaskScripts.addLast(ScriptedCall.NetworkFailure)
    }

    override suspend fun getTasks(): List<Task> = nextScript(getTasksScripts, "GET /v1/tasks").asTasks()

    override suspend fun getTask(taskId: String): Task =
        nextScript(getTaskScripts, "GET /v1/tasks/$taskId").asTask()

    override suspend fun isValid(taskId: String): Boolean =
        nextScript(isValidScripts, "GET /v1/tasks/$taskId/valid").asBoolean()

    override suspend fun createTask(request: TaskRequest): Task {
        createTaskRequests.add(request)
        return nextScript(createTaskScripts, "POST /v1/tasks").asTask()
    }

    override suspend fun updateTask(taskId: String, request: TaskRequest): Task {
        updateTaskRequests.add(RecordedUpdate(taskId, request))
        return nextScript(updateTaskScripts, "PUT /v1/tasks/$taskId").asTask()
    }

    override suspend fun deleteTask(taskId: String): Response<Unit> {
        deletedTaskIds.add(taskId)
        return nextScript(deleteTaskScripts, "DELETE /v1/tasks/$taskId").asDeleteResponse()
    }

    private fun nextScript(scripts: ArrayDeque<ScriptedCall>, label: String): ScriptedCall =
        scripts.poll() ?: error("No scripted response for $label")

    private sealed interface ScriptedCall {
        data class Success(val body: Any) : ScriptedCall
        data class Error(val code: Int, val message: String?) : ScriptedCall
        data object EmptySuccess : ScriptedCall
        data object NetworkFailure : ScriptedCall

        fun asTasks(): List<Task> = when (this) {
            is Success -> body as List<Task>
            is Error -> throw httpException(code, message)
            EmptySuccess -> error("Unexpected empty response for tasks list")
            NetworkFailure -> throw networkException()
        }

        fun asTask(): Task = when (this) {
            is Success -> body as Task
            is Error -> throw httpException(code, message)
            EmptySuccess -> error("Unexpected empty response for task")
            NetworkFailure -> throw networkException()
        }

        fun asBoolean(): Boolean = when (this) {
            is Success -> body as Boolean
            is Error -> throw httpException(code, message)
            EmptySuccess -> error("Unexpected empty response for validation")
            NetworkFailure -> throw networkException()
        }

        fun asDeleteResponse(): Response<Unit> = when (this) {
            EmptySuccess -> Response.success(Unit)
            is Error -> Response.error(code, errorBody(code, message))
            is Success -> error("Unexpected success body for delete")
            NetworkFailure -> throw networkException()
        }

        private fun httpException(code: Int, message: String?): HttpException {
            val response = Response.error<String>(code, errorBody(code, message))
            return HttpException(response)
        }

        private fun networkException(): IOException = IOException("Network request failed")

        private fun errorBody(code: Int, message: String?): okhttp3.ResponseBody =
            if (message.isNullOrBlank()) {
                "".toResponseBody("application/json".toMediaType())
            } else {
                """{"message":"$message"}""".toResponseBody("application/json".toMediaType())
            }
    }
}
