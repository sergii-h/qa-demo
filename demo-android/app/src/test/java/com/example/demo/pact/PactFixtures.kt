package com.example.demo.pact

import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.DslPart
import au.com.dius.pact.consumer.dsl.LambdaDsl
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
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
        .valueFromProviderState("title", "taskTitle", createTaskRequest().title)
        .stringValue("description", createTaskRequest().description!!)
        .stringValue("status", createTaskRequest().status.name)
        .stringValue("priority", createTaskRequest().priority.name)

    fun updateTaskRequestBody(): DslPart = PactDslJsonBody()
        .valueFromProviderState("title", "updatedTitle", updateTaskRequest().title)
        .stringValue("description", updateTaskRequest().description!!)
        .stringValue("status", updateTaskRequest().status.name)
        .stringValue("priority", updateTaskRequest().priority.name)

    fun taskResponseBody(titleExample: String = "Prepare release notes"): DslPart = PactDslJsonBody()
        .stringMatcher("id", "^[a-f0-9]{24}$", TASK_ID)
        .stringType("title", titleExample)
        .stringType("description", "Document release tasks")
        .stringType("status", TaskStatus.TODO.name)
        .stringType("priority", TaskPriority.MEDIUM.name)
        .stringMatcher("createdDate", TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
        .stringMatcher("updatedDate", TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")

    fun taskListResponseBody(): DslPart = LambdaDsl.newJsonArrayMinLike(1) { array ->
        array.`object` { item ->
            item.stringMatcher("id", "^[a-f0-9]{24}$", TASK_ID)
            item.stringType("title", "Prepare release notes")
            item.stringType("description", "Document release tasks")
            item.stringType("status", TaskStatus.TODO.name)
            item.stringType("priority", TaskPriority.MEDIUM.name)
            item.stringMatcher("createdDate", TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
            item.stringMatcher("updatedDate", TIMESTAMP_PATTERN, "2026-04-26T09:00:00.000Z")
        }
    }.build()

    fun duplicateTitleErrorBody(exampleMessage: String): DslPart = PactDslJsonBody()
        .stringMatcher(
            "message",
            "^Task with title '.*' already exists$",
            exampleMessage
        )
}
