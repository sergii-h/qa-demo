import Foundation

protocol TaskRepositoryProtocol: Sendable {
    func getTasks() async throws -> [Task]
    func getTask(id: String) async throws -> Task
    func isValid(id: String) async throws -> Bool
    func createTask(_ request: TaskRequest) async throws -> Task
    func updateTask(id: String, request: TaskRequest) async throws -> Task
    func deleteTask(id: String) async throws
}

final class TaskRepository: TaskRepositoryProtocol {
    private let api: TaskAPI

    init(api: TaskAPI) {
        self.api = api
    }

    func getTasks() async throws -> [Task] {
        try await api.getTasks()
    }

    func getTask(id: String) async throws -> Task {
        try await api.getTask(id: id)
    }

    func isValid(id: String) async throws -> Bool {
        try await api.isValid(id: id)
    }

    func createTask(_ request: TaskRequest) async throws -> Task {
        try await api.createTask(request)
    }

    func updateTask(id: String, request: TaskRequest) async throws -> Task {
        try await api.updateTask(id: id, request: request)
    }

    func deleteTask(id: String) async throws {
        try await api.deleteTask(id: id)
    }
}
