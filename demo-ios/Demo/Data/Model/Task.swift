import Foundation

enum TaskValidation {
    static let maxTitleLength = 100
}

struct Task: Codable, Equatable, Identifiable, Sendable {
    let id: String
    let title: String
    let description: String?
    let status: TaskStatus
    let priority: TaskPriority
    let createdDate: String?
    let updatedDate: String?
}

struct TaskRequest: Codable, Equatable, Sendable {
    let title: String
    let description: String?
    let status: TaskStatus
    let priority: TaskPriority
}

struct ErrorResponse: Codable, Equatable, Sendable {
    let message: String?
}
