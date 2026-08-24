import Foundation

struct TaskResponse: Codable, Equatable {
    let id: String
    let title: String
    let description: String
    let status: TaskStatus
    let priority: TaskPriority
    let createdDate: String
    let updatedDate: String

    func jsonObject() throws -> Any {
        let data = try JSONEncoder().encode(self)
        return try JSONSerialization.jsonObject(with: data)
    }
}
