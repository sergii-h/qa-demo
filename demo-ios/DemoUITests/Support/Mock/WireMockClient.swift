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
        let actualState = try fetchScenarioState(scenarioName)
        let plannedState = plannedScenarioStates[scenarioName] ?? "Started"
        let requiredState = Self.laterScenarioState(actualState, plannedState)
        let newState = Self.nextScenarioState(requiredState)
        plannedScenarioStates[scenarioName] = newState

        let mapping: [String: Any] = [
            "scenarioName": scenarioName,
            "requiredScenarioState": requiredState,
            "newScenarioState": newState,
            "request": request,
            "response": response,
        ]
        try requestAdmin(
            method: "POST",
            path: "/__admin/mappings",
            body: try JSONSerialization.data(withJSONObject: mapping)
        )
    }

    func addOverrideMapping(
        request: [String: String],
        response: [String: Any]
    ) throws {
        let mapping: [String: Any] = [
            "priority": 1,
            "request": request,
            "response": response,
        ]
        try requestAdmin(
            method: "POST",
            path: "/__admin/mappings",
            body: try JSONSerialization.data(withJSONObject: mapping)
        )
    }

    private func fetchScenarioState(_ scenarioName: String) throws -> String {
        let data = try requestAdmin(method: "GET", path: "/__admin/scenarios")
        guard
            let json = try JSONSerialization.jsonObject(with: data) as? [String: Any],
            let scenarios = json["scenarios"] as? [[String: Any]]
        else {
            return "Started"
        }
        return scenarios.first { $0["name"] as? String == scenarioName }
            .flatMap { $0["state"] as? String }
            ?? "Started"
    }

    @discardableResult
    private func requestAdmin(method: String, path: String, body: Data? = nil) throws -> Data {
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
        var completed: Result<Data, Error> = .failure(WireMockClientError.timeout)
        URLSession.shared.dataTask(with: request) { data, response, error in
            if let error {
                completed = .failure(error)
            } else if let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) {
                completed = .success(data ?? Data())
            } else {
                let status = (response as? HTTPURLResponse)?.statusCode ?? -1
                completed = .failure(WireMockClientError.httpStatus(status))
            }
            semaphore.signal()
        }.resume()

        if semaphore.wait(timeout: .now() + 10) == .timedOut {
            throw WireMockClientError.timeout
        }
        return try completed.get()
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

    private static func laterScenarioState(_ left: String, _ right: String) -> String {
        scenarioStep(left) >= scenarioStep(right) ? left : right
    }

    private static func scenarioStep(_ state: String) -> Int {
        if state == "Started" {
            return 0
        }
        return Int(state.replacingOccurrences(of: "step-", with: "")) ?? 0
    }

    private static func nextScenarioState(_ current: String) -> String {
        if current == "Started" {
            return "step-1"
        }
        return "step-\(scenarioStep(current) + 1)"
    }
}

enum WireMockClientError: Error {
    case invalidURL
    case invalidJSON
    case timeout
    case httpStatus(Int)
}
