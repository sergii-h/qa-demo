import Foundation

final class WireMockClient {
    private let baseURL: URL
    private var plannedScenarioStates: [String: String] = [:]
    private let session: URLSession = {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.timeoutIntervalForRequest = 10
        configuration.timeoutIntervalForResource = 10
        configuration.waitsForConnectivity = false
        return URLSession(configuration: configuration)
    }()

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

        let outcome = RequestOutcome()
        let task = session.dataTask(with: request) { data, response, error in
            if let error {
                outcome.finish(.failure(error))
            } else if let http = response as? HTTPURLResponse, (200..<300).contains(http.statusCode) {
                outcome.finish(.success(data ?? Data()))
            } else {
                let status = (response as? HTTPURLResponse)?.statusCode ?? -1
                outcome.finish(.failure(WireMockClientError.httpStatus(status)))
            }
        }
        task.resume()

        let deadline = Date().addingTimeInterval(10)
        while outcome.result == nil, Date() < deadline {
            RunLoop.current.run(mode: .default, before: Date().addingTimeInterval(0.05))
        }
        guard let result = outcome.result else {
            task.cancel()
            throw WireMockClientError.timeout
        }
        return try result.get()
    }

    static func resolveBaseURL() -> URL {
        if let override = ProcessInfo.processInfo.environment["WIREMOCK_URL"],
           let url = URL(string: override.trimmingCharacters(in: CharacterSet(charactersIn: "/"))) {
            return preferringIPv4Loopback(url)
        }
        if let api = ProcessInfo.processInfo.environment["API_BASE_URL"],
           let url = URL(string: api),
           var components = URLComponents(url: url, resolvingAgainstBaseURL: false) {
            components.path = ""
            components.query = nil
            if let host = components.string,
               let resolved = URL(string: host.trimmingCharacters(in: CharacterSet(charactersIn: "/"))) {
                return preferringIPv4Loopback(resolved)
            }
        }
        return URL(string: "http://127.0.0.1:8085")!
    }

    private static func preferringIPv4Loopback(_ url: URL) -> URL {
        guard var components = URLComponents(url: url, resolvingAgainstBaseURL: false),
              components.host == "localhost" else {
            return url
        }
        components.host = "127.0.0.1"
        return components.url ?? url
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

private final class RequestOutcome: @unchecked Sendable {
    private let lock = NSLock()
    private var stored: Result<Data, Error>?

    var result: Result<Data, Error>? {
        lock.lock()
        defer { lock.unlock() }
        return stored
    }

    func finish(_ result: Result<Data, Error>) {
        lock.lock()
        stored = result
        lock.unlock()
    }
}

enum WireMockClientError: Error {
    case invalidURL
    case invalidJSON
    case timeout
    case httpStatus(Int)
}
