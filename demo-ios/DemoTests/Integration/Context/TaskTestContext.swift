import Foundation
@testable import Demo

struct TaskUpdateRequest: Equatable {
    let taskId: String
    let request: TaskRequest
}

struct TaskTestContext {
    var id: String
    var title: String
    var description: String?
    var status: TaskStatus
    var priority: TaskPriority
    var createdDate: String?
    var updatedDate: String?

    init(
        id: String = UUID().uuidString,
        title: String = TaskTestContext.randomAlphabetic(12),
        description: String? = TaskTestContext.randomAlphabetic(12),
        status: TaskStatus = .todo,
        priority: TaskPriority = .medium,
        createdDate: String? = "2024-01-15T10:00:00.000Z",
        updatedDate: String? = "2024-01-16T12:00:00.000Z"
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.status = status
        self.priority = priority
        self.createdDate = createdDate
        self.updatedDate = updatedDate
    }

    func createTaskResponse() -> Task {
        Task(
            id: id,
            title: title,
            description: description,
            status: status,
            priority: priority,
            createdDate: createdDate,
            updatedDate: updatedDate
        )
    }

    func createTaskRequest() -> TaskRequest {
        TaskRequest(
            title: title,
            description: description,
            status: status,
            priority: priority
        )
    }

    func createTaskUpdateRequest() -> TaskUpdateRequest {
        TaskUpdateRequest(taskId: id, request: createTaskRequest())
    }

    private static func randomAlphabetic(_ length: Int) -> String {
        String((0..<length).map { _ in Character(UnicodeScalar(Int.random(in: 65...90))!) })
    }
}
