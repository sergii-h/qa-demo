package com.example.demo.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.LambdaDsl
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import com.example.demo.data.model.ErrorResponse
import com.example.demo.data.model.Task
import com.example.demo.data.model.TaskPriority
import com.example.demo.data.model.TaskRequest
import com.example.demo.data.model.TaskStatus
import com.example.demo.data.remote.ApiClient
import com.example.demo.data.remote.TaskApi

object PactFixtures {
    const val CONSUMER = "demo-android"
    const val TASK_ID = "507f1f77bcf86cd799439011"
    const val TIMESTAMP_PATTERN = "^\\d{4}-\\d{2}-\\d{2}T.*$"

    fun createTaskApi(mockServer: MockServer): TaskApi =
        ApiClient.createTaskApi("${mockServer.getUrl()}/v1/")

    fun createTaskRequest(): TaskRequest = TaskRequest(
        title = "Prepare release notes",
        description = "Document release tasks",
        status = TaskStatus.TODO,
        priority = TaskPriority.MEDIUM
    )

    fun updateTaskRequest(): TaskRequest = TaskRequest(
        title = "Prepare release notes - updated",
        description = "Document release tasks in detail",
        status = TaskStatus.IN_PROGRESS,
        priority = TaskPriority.HIGH
    )

    fun createTaskRequestBody(): DslPart = PactDslJsonBody()
        .valueFromProviderState(TaskRequest::title.name, "taskTitle", createTaskRequest().title)
        .stringValue(TaskRequest::description.name, createTaskRequest().description!!)
        .stringValue(TaskRequest::status.name, createTaskRequest().status.name)
        .stringValue(TaskRequest::priority.name, createTaskRequest().priority.name)

    fun updateTaskRequestBody(): DslPart = PactDslJsonBody()
        .valueFromProviderState(TaskRequest::title.name, "updatedTitle", updateTaskRequest().title)
        .stringValue(TaskRequest::description.name, updateTaskRequest().description!!)
        .stringValue(TaskRequest::status.name, updateTaskRequest().status.name)
        .stringValue(TaskRequest::priority.name, updateTaskRequest().priority.name)

    fun taskResponseBody(titleExample: String = "Prepare release notes"): DslPart = PactDslJsonBody()
        .stringMatcher(Task::id.name, "^[a-f0-9]{24}$", TASK_ID)
        .stringType(Task::title.name, titleExample)
        .stringType(Task::description.name, "Document release tasks")
        .stringType(Task::status.name, TaskStatus.TODO.name)
        .stringType(Task::priority.name, TaskPriority.MEDIUM.name)
        .stringMatcher(Task::createdDate.name, TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
        .stringMatcher(Task::updatedDate.name, TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")

    fun taskListResponseBody(): DslPart = LambdaDsl.newJsonArrayMinLike(1) { array ->
        array.`object` { item ->
            item.stringMatcher(Task::id.name, "^[a-f0-9]{24}$", TASK_ID)
            item.stringType(Task::title.name, "Prepare release notes")
            item.stringType(Task::description.name, "Document release tasks")
            item.stringType(Task::status.name, TaskStatus.TODO.name)
            item.stringType(Task::priority.name, TaskPriority.MEDIUM.name)
            item.stringMatcher(Task::createdDate.name, TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
            item.stringMatcher(Task::updatedDate.name, TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
        }
    }.build()

    fun duplicateTitleErrorBody(exampleMessage: String): DslPart = PactDslJsonBody()
        .stringMatcher(
            ErrorResponse::message.name,
            "^Task with title '.*' already exists$",
            exampleMessage
        )
}
