import Foundation
@testable import Demo

enum TaskFixtures {
    static let sampleTask = Task(
        id: "task-1",
        title: "Buy milk",
        description: "2%",
        status: .todo,
        priority: .medium,
        createdDate: "2024-01-01T10:00:00Z",
        updatedDate: "2024-01-02T10:00:00Z"
    )

    static let sampleRequest = TaskRequest(
        title: "Buy milk",
        description: "2%",
        status: .todo,
        priority: .medium
    )
}

final class MockTaskAPI: TaskAPI, @unchecked Sendable {
    var tasks: [Task] = []
    var taskById: [String: Task] = [:]
    var validById: [String: Bool] = [:]
    var createHandler: ((TaskRequest) throws -> Task)?
    var updateHandler: ((String, TaskRequest) throws -> Task)?
    var deleteHandler: ((String) throws -> Void)?
    var getTasksError: Error?
    var getTaskError: Error?
    var isValidError: Error?
    var getTasksDelayMilliseconds: Int = 0
    var createTaskDelayMilliseconds: Int = 0

    func getTasks() async throws -> [Task] {
        if getTasksDelayMilliseconds > 0 {
            try await _Concurrency.Task.sleep(for: .milliseconds(getTasksDelayMilliseconds))
        }
        if let getTasksError { throw getTasksError }
        return tasks
    }

    func getTask(id: String) async throws -> Task {
        if let getTaskError { throw getTaskError }
        guard let task = taskById[id] else {
            throw APIErrorResponse(statusCode: 404, body: nil)
        }
        return task
    }

    func isValid(id: String) async throws -> Bool {
        if let isValidError { throw isValidError }
        return validById[id] ?? false
    }

    func createTask(_ request: TaskRequest) async throws -> Task {
        if createTaskDelayMilliseconds > 0 {
            try await _Concurrency.Task.sleep(for: .milliseconds(createTaskDelayMilliseconds))
        }
        if let createHandler { return try createHandler(request) }
        let task = Task(
            id: "created-id",
            title: request.title,
            description: request.description,
            status: request.status,
            priority: request.priority,
            createdDate: "2024-01-01T10:00:00Z",
            updatedDate: "2024-01-01T10:00:00Z"
        )
        tasks.append(task)
        taskById[task.id] = task
        return task
    }

    func updateTask(id: String, request: TaskRequest) async throws -> Task {
        if let updateHandler { return try updateHandler(id, request) }
        let task = Task(
            id: id,
            title: request.title,
            description: request.description,
            status: request.status,
            priority: request.priority,
            createdDate: "2024-01-01T10:00:00Z",
            updatedDate: "2024-01-02T10:00:00Z"
        )
        taskById[id] = task
        return task
    }

    func deleteTask(id: String) async throws {
        if let deleteHandler {
            try deleteHandler(id)
            return
        }
        tasks.removeAll { $0.id == id }
        taskById.removeValue(forKey: id)
    }
}

@MainActor
enum AppLocaleTestSupport {
    static func resetToEnglish() {
        AppLocale.shared.setLanguage(AppLocale.english)
    }
}
