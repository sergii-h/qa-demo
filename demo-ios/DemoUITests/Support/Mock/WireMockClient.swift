import Foundation

final class WireMockClient {
    private let baseURL: URL
    private var plannedScenarioStates: [String: String] = [:]

    init(baseURL: URL = WireMockClient.resolveBaseURL()) {
        self.baseURL = baseURL
    }

    func reset() throws {
        try requestAdmin(method: "DELETE", path: "/__admin/mappings")
        try requestAdmin(method: "POST", path: "/__admin/scenarios/reset", body: Data("{}".utf8))
        plannedScenarioStates.removeAll()
    }

    func addScenarioMapping(
        scenarioName: String,
        request: [String: String],
        response: [String: Any]
    ) throws {
        let requiredState = plannedScenarioStates[scenarioName] ?? "Started"
        let newState = Self.nextScenarioState(requiredState)
        plannedScenarioStates[scenarioName] = newState

        let mapping: [String: Any] = [
            "scenarioName": scenarioName,
            "requiredScenarioState": requiredState,
            "newScenarioState": newState,
            "request": request,
            "response": response,
        ]
        try requestAdmin(method: "POST", path: "/__admin/mappings", body: try JSONSerialization.data(withJSONObject: mapping))
    }

    private func requestAdmin(method: String, path: String, body: Data? = nil) throws {
        guard let url = URL(string: path, relativeTo: baseURL) else {
            throw WireMockClientError.invalidURL
        }
        var request = URLRequest(url: url)
        request.httpMethod = method
        if let body {
            request.setValue("application/json", forHTTPHeaderField: "Content-Type")
            request.httpBody = body
        }

        let semaphore = DispatchSemaphore(value: 0)
        var completed: Result<Void, Error> = .failure(WireMockClientError.timeout)
        URLSession.shared.dataTask(with: request) { _, response, error in
            if let error {
                completed = .failure(error)
            } else if let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) {
                completed = .success(())
            } else {
                let status = (response as? HTTPURLResponse)?.statusCode ?? -1
                completed = .failure(WireMockClientError.httpStatus(status))
            }
            semaphore.signal()
        }.resume()

        if semaphore.wait(timeout: .now() + 10) == .timedOut {
            throw WireMockClientError.timeout
        }
        try completed.get()
    }

    static func resolveBaseURL() -> URL {
        if let override = ProcessInfo.processInfo.environment["WIREMOCK_URL"],
           let url = URL(string: override.trimmingCharacters(in: CharacterSet(charactersIn: "/"))) {
            return url
        }
        if let api = ProcessInfo.processInfo.environment["API_BASE_URL"],
           let url = URL(string: api),
           var components = URLComponents(url: url, resolvingAgainstBaseURL: false) {
            components.path = ""
            components.query = nil
            if let host = components.string {
                return URL(string: host.trimmingCharacters(in: CharacterSet(charactersIn: "/")))!
            }
        }
        return URL(string: "http://localhost:8085")!
    }

    private static func nextScenarioState(_ current: String) -> String {
        if current == "Started" {
            return "step-1"
        }
        let step = Int(current.replacingOccurrences(of: "step-", with: "")) ?? 0
        return "step-\(step + 1)"
    }
}

enum WireMockClientError: Error {
    case invalidURL
    case invalidJSON
    case timeout
    case httpStatus(Int)
}

final class ApiRouteMock {
    private let wireMock: WireMockClient

    init(wireMock: WireMockClient) {
        self.wireMock = wireMock
    }

    @discardableResult
    func getTasks(_ tasks: [[String: Any]] = []) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "get-tasks",
            request: ["method": "GET", "urlPath": "/v1/tasks"],
            response: jsonResponse(200, tasks)
        )
        return self
    }

    @discardableResult
    func getTask(_ task: [String: Any]) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "get-task",
            request: ["method": "GET", "urlPathPattern": "/v1/tasks/(?!isValid)[^/]+"],
            response: jsonResponse(200, task)
        )
        return self
    }

    @discardableResult
    func getIsValid(_ isValid: Bool) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "is-valid",
            request: ["method": "GET", "urlPathPattern": "/v1/tasks/isValid/.+"],
            response: jsonResponse(200, isValid)
        )
        return self
    }

    @discardableResult
    func createTask(_ task: [String: Any]) throws -> ApiRouteMock {
        try wireMock.addScenarioMapping(
            scenarioName: "create-task",
            request: ["method": "POST", "urlPath": "/v1/tasks"],
            response: jsonResponse(200, task)
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
}

final class ApiRouteMockClient {
    private let wireMock = WireMockClient()
    private var apiMock: ApiRouteMock?

    func api() -> ApiRouteMock {
        if apiMock == nil {
            apiMock = ApiRouteMock(wireMock: wireMock)
        }
        return apiMock!
    }

    func start() throws {
        try reset()
        try api().getTasks()
    }

    func reset() throws {
        try wireMock.reset()
        apiMock = nil
    }
}
