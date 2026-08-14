import Foundation

enum APIClient {
    static func makeTaskAPI(baseURL: URL, session: URLSession = .shared) -> TaskAPI {
        URLSessionTaskAPI(baseURL: baseURL, session: session)
    }
}

final class URLSessionTaskAPI: TaskAPI {
    private let baseURL: URL
    private let session: URLSession
    private let decoder = JSONDecoder()
    private let encoder = JSONEncoder()

    init(baseURL: URL, session: URLSession) {
        self.baseURL = baseURL
        self.session = session
    }

    func getTasks() async throws -> [Task] {
        try await send(request: makeRequest(path: "tasks", method: "GET"))
    }

    func getTask(id: String) async throws -> Task {
        try await send(request: makeRequest(path: "tasks/\(id)", method: "GET"))
    }

    func isValid(id: String) async throws -> Bool {
        try await send(request: makeRequest(path: "tasks/isValid/\(id)", method: "GET"))
    }

    func createTask(_ request: TaskRequest) async throws -> Task {
        try await send(
            request: makeRequest(path: "tasks", method: "POST", body: request),
            expectedStatusCodes: [200, 201]
        )
    }

    func updateTask(id: String, request: TaskRequest) async throws -> Task {
        try await send(request: makeRequest(path: "tasks/\(id)", method: "PUT", body: request))
    }

    func deleteTask(id: String) async throws {
        let _: EmptyResponse = try await send(
            request: makeRequest(path: "tasks/\(id)", method: "DELETE"),
            expectedStatusCodes: [200, 204]
        )
    }

    private func makeRequest<T: Encodable>(
        path: String,
        method: String,
        body: T? = nil as EmptyResponse?
    ) throws -> URLRequest {
        guard let url = URL(string: path, relativeTo: baseURL) else {
            throw APIError.invalidURL
        }
        var request = URLRequest(url: url)
        request.httpMethod = method
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        if let body {
            request.httpBody = try encoder.encode(body)
        }
        return request
    }

    private func send<T: Decodable>(
        request: URLRequest,
        expectedStatusCodes: Set<Int> = [200]
    ) async throws -> T {
        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse else {
            throw APIError.invalidResponse
        }
        guard expectedStatusCodes.contains(httpResponse.statusCode) else {
            throw APIErrorResponse(statusCode: httpResponse.statusCode, body: String(data: data, encoding: .utf8))
        }
        if T.self == EmptyResponse.self {
            return EmptyResponse() as! T
        }
        do {
            return try decoder.decode(T.self, from: data)
        } catch {
            throw error
        }
    }
}

private struct EmptyResponse: Codable, Equatable {
    init() {}
}
