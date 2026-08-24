import Foundation

final class ApiRouteMock {
    private let wireMock: WireMockClient

    init(wireMock: WireMockClient) {
        self.wireMock = wireMock
    }

    @discardableResult
    func getTasks(_ tasks: TaskResponse...) throws -> ApiRouteMock {
        let bodies = try tasks.map { try $0.jsonObject() }
        try wireMock.addScenarioMapping(
            scenarioName: "get-tasks",
            request: ["method": "GET", "urlPath": Self.tasksPath],
            response: try jsonResponse(200, bodies)
        )
        return self
    }

    @discardableResult
    func getTask(_ task: TaskResponse) throws -> ApiRouteMock {
        try wireMock.addOverrideMapping(
            request: ["method": "GET", "urlPathPattern": Self.taskByIdPath],
            response: try jsonResponse(200, task.jsonObject())
        )
        return self
    }

    @discardableResult
    func getIsValid(_ isValid: Bool) throws -> ApiRouteMock {
        try wireMock.addOverrideMapping(
            request: ["method": "GET", "urlPathPattern": Self.isValidPath],
            response: try jsonResponse(200, isValid)
        )
        return self
    }

    @discardableResult
    func createTask(_ task: TaskResponse) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "create-task",
            request: ["method": "POST", "urlPath": Self.tasksPath],
            response: try jsonResponse(200, task.jsonObject())
        )
        return self
    }

    @discardableResult
    func updateTask(_ task: TaskResponse) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "update-task",
            request: ["method": "PUT", "urlPathPattern": Self.taskByIdPath],
            response: try jsonResponse(200, task.jsonObject())
        )
        return self
    }

    @discardableResult
    func deleteTask() throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "delete-task",
            request: ["method": "DELETE", "urlPathPattern": Self.taskByIdPath],
            response: ["status": 204]
        )
        return self
    }

    private func jsonResponse(_ status: Int, _ body: Any) throws -> [String: Any] {
        let json: String
        if JSONSerialization.isValidJSONObject(body) {
            let data = try JSONSerialization.data(withJSONObject: body)
            json = String(data: data, encoding: .utf8) ?? "null"
        } else if let flag = body as? Bool {
            json = flag ? "true" : "false"
        } else {
            throw WireMockClientError.invalidJSON
        }
        return [
            "status": status,
            "body": json,
            "headers": ["Content-Type": "application/json"],
        ]
    }

    private static let tasksPath = "/v1/tasks"
    private static let taskByIdPath = "/v1/tasks/(?!isValid)[^/]+"
    private static let isValidPath = "/v1/tasks/isValid/.+"
}
