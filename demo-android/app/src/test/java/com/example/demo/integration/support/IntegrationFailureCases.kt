package com.example.demo.integration.support

enum class CreatePostFailure(
    val label: String,
    val expectedSaveError: String,
) {
    HTTP_400("HTTP 400", "Invalid task data"),
    HTTP_500("HTTP 500", "Request failed (500)"),
    NETWORK("network error", "End of input");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_400 -> server.enqueueCreateTaskError(400)
            HTTP_500 -> server.enqueueCreateTaskError(500)
            NETWORK -> server.enqueueCreateTaskNetworkFailure()
        }
    }
}

enum class UpdatePutFailure(
    val label: String,
    val expectedSaveError: String,
) {
    HTTP_400("HTTP 400", "Invalid task data"),
    HTTP_500("HTTP 500", "Request failed (500)"),
    NETWORK("network error", "End of input");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_400 -> server.enqueueUpdateTaskError(400)
            HTTP_500 -> server.enqueueUpdateTaskError(500)
            NETWORK -> server.enqueueUpdateTaskNetworkFailure()
        }
    }
}

enum class DeleteFailure(val label: String) {
    HTTP_500("HTTP 500"),
    NETWORK("network error");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_500 -> server.enqueueDeleteError(500, "Delete failed")
            NETWORK -> server.enqueueDeleteNetworkFailure()
        }
    }
}

enum class GetTasksFailure(val label: String) {
    HTTP_500("HTTP 500"),
    NETWORK("network error");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_500 -> server.enqueueGetTasksError(500)
            NETWORK -> server.enqueueGetTasksNetworkFailure()
        }
    }
}

enum class GetTaskFailure(
    val label: String,
    val expectedLoadError: String,
) {
    HTTP_500("HTTP 500", "Request failed (500)"),
    NETWORK("network error", "End of input");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_500 -> server.enqueueGetTaskError(500)
            NETWORK -> server.enqueueGetTaskNetworkFailure()
        }
    }
}

enum class IsValidFailure(val label: String) {
    HTTP_500("HTTP 500"),
    NETWORK("network error");

    fun enqueue(server: IntegrationMockServer) {
        when (this) {
            HTTP_500 -> server.enqueueIsValidError(500, "Validation failed")
            NETWORK -> server.enqueueIsValidNetworkFailure()
        }
    }
}
