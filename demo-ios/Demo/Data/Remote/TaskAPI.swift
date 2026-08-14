import Foundation

protocol TaskAPI: Sendable {
    func getTasks() async throws -> [Task]
    func getTask(id: String) async throws -> Task
    func isValid(id: String) async throws -> Bool
    func createTask(_ request: TaskRequest) async throws -> Task
    func updateTask(id: String, request: TaskRequest) async throws -> Task
    func deleteTask(id: String) async throws
}
