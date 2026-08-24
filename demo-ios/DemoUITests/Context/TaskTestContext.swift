import Foundation

struct TaskTestContext {
    let id: String
    let title: String
    let description: String
    let status: TaskStatus
    let priority: TaskPriority

    init(
        id: String = UUID().uuidString,
        title: String = TaskTestContext.randomAlphabetic(12),
        description: String = TaskTestContext.randomAlphabetic(12),
        status: TaskStatus = .todo,
        priority: TaskPriority = .medium
    ) {
        self.id = id
        self.title = title
        self.description = description
        self.status = status
        self.priority = priority
    }

    func createTaskData() -> TaskData {
        TaskData(
            title: title,
            description: description,
            status: status,
            priority: priority
        )
    }

    func createTaskResponse() -> TaskResponse {
        TaskResponse(
            id: id,
            title: title,
            description: description,
            status: status,
            priority: priority,
            createdDate: Self.mockCreatedDate,
            updatedDate: Self.mockUpdatedDate
        )
    }

    func copy(
        title: String? = nil,
        description: String? = nil,
        status: TaskStatus? = nil,
        priority: TaskPriority? = nil
    ) -> TaskTestContext {
        TaskTestContext(
            id: id,
            title: title ?? self.title,
            description: description ?? self.description,
            status: status ?? self.status,
            priority: priority ?? self.priority
        )
    }

    private static let mockCreatedDate = "2024-01-01T10:00:00Z"
    private static let mockUpdatedDate = "2024-01-02T10:00:00Z"

    private static func randomAlphabetic(_ length: Int) -> String {
        String((0..<length).map { _ in "ABCDEFGHIJKLMNOPQRSTUVWXYZ".randomElement()! })
    }
}
