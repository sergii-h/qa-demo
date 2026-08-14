import Foundation
import os
@testable import Demo

final class IntegrationMockServer {
    private(set) var createTaskRequests: [TaskRequest] = []
    private(set) var updateTaskRequests: [(String, TaskRequest)] = []
    private(set) var deleteTaskIds: [String] = []
    private(set) var getTasksRequestCount = 0
    private(set) var getTaskRequestCount = 0
    private var lastSuccessfulGetTasks: [Task] = []
    private var responses: [(URLRequest) throws -> (HTTPURLResponse, Data)] = []
    private var isStarted = false
    private var session: URLSession?
    private let inFlightStubRequests = OSAllocatedUnfairLock(initialState: 0)

    func hasInFlightStubRequests() -> Bool {
        inFlightStubRequests.withLock { $0 > 0 }
    }

    func start() {
        isStarted = true
        StubURLProtocol.requestHandler = { [self] request in
            inFlightStubRequests.withLock { $0 += 1 }
            defer { inFlightStubRequests.withLock { $0 -= 1 } }
            return try dispatch(request)
        }
    }

    private func dispatch(_ request: URLRequest) throws -> (HTTPURLResponse, Data) {
        var index = 0
        while index < responses.count {
            let handler = responses[index]
            do {
                let response = try handler(request)
                responses.remove(at: index)
                return response
            } catch let error as APIError {
                switch error {
                case .invalidURL, .invalidResponse:
                    index += 1
                default:
                    responses.remove(at: index)
                    throw error
                }
            } catch {
                responses.remove(at: index)
                throw error
            }
        }
        if request.httpMethod == "GET", request.url?.path == "/v1/tasks" {
            let data = try JSONEncoder().encode(lastSuccessfulGetTasks)
            return (
                HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!,
                data
            )
        }
        throw APIError.invalidResponse
    }

    func shutdown() {
        if isStarted {
            StubURLProtocol.requestHandler = nil
        }
        drainInFlightStubRequests()
        session = nil
        isStarted = false
        responses = []
        createTaskRequests = []
        updateTaskRequests = []
        deleteTaskIds = []
        getTasksRequestCount = 0
        getTaskRequestCount = 0
        lastSuccessfulGetTasks = []
    }

    private func drainInFlightStubRequests() {
        for _ in 0..<400 {
            if inFlightStubRequests.withLock({ $0 }) == 0 {
                return
            }
            RunLoop.current.run(until: Date(timeIntervalSinceNow: 0.01))
        }
    }

    func makeRepository() -> TaskRepository {
        let baseURL = URL(string: "http://stub.test/v1/")!
        let session = StubURLSessionFactory.makeSession()
        self.session = session
        return TaskRepository(api: APIClient.makeTaskAPI(baseURL: baseURL, session: session))
    }

    @discardableResult
    func enqueueGetTasks(_ tasks: [Task] = []) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertPath(request, "/v1/tasks")
            self.getTasksRequestCount += 1
            self.lastSuccessfulGetTasks = tasks
            let data = try JSONEncoder().encode(tasks)
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueGetTasksError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertPath(request, "/v1/tasks")
            self.getTasksRequestCount += 1
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueGetTasksNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertPath(request, "/v1/tasks")
            self.getTasksRequestCount += 1
            throw URLError(.notConnectedToInternet)
        }
        return self
    }

    @discardableResult
    func enqueueGetTasksForLanguageSwitch(_ tasks: [Task] = []) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertPath(request, "/v1/tasks")
            self.getTasksRequestCount += 1
            self.lastSuccessfulGetTasks = tasks
            let data = try JSONEncoder().encode(tasks)
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueGetTask(_ task: Task) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertTaskPath(request)
            self.getTaskRequestCount += 1
            let data = try JSONEncoder().encode(task)
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueGetTaskError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertTaskPath(request)
            self.getTaskRequestCount += 1
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueGetTaskNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertTaskPath(request)
            self.getTaskRequestCount += 1
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueIsValid(_ isValid: Bool) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertIsValidPath(request)
            let data = try JSONEncoder().encode(isValid)
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueIsValidError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertIsValidPath(request)
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueIsValidNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "GET")
            try self.assertIsValidPath(request)
            throw URLError(.notConnectedToInternet)
        }
        return self
    }

    @discardableResult
    func enqueueCreateTask(_ task: Task) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "POST")
            try self.assertPath(request, "/v1/tasks")
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.createTaskRequests.append(requestBody)
            let data = try JSONEncoder().encode(task)
            return (HTTPURLResponse(url: request.url!, statusCode: 201, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueCreateTaskError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "POST")
            try self.assertPath(request, "/v1/tasks")
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.createTaskRequests.append(requestBody)
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueCreateTaskNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "POST")
            try self.assertPath(request, "/v1/tasks")
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.createTaskRequests.append(requestBody)
            throw URLError(.notConnectedToInternet)
        }
        return self
    }

    @discardableResult
    func enqueueUpdateTask(_ task: Task) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "PUT")
            try self.assertPath(request, "/v1/tasks/\(task.id)")
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.updateTaskRequests.append((task.id, requestBody))
            let data = try JSONEncoder().encode(task)
            return (HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!, data)
        }
        return self
    }

    @discardableResult
    func enqueueUpdateTaskError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "PUT")
            let taskId = try self.taskId(from: request)
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.updateTaskRequests.append((taskId, requestBody))
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueUpdateTaskNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "PUT")
            let taskId = try self.taskId(from: request)
            let body = self.requestBody(from: request)
            let requestBody = try JSONDecoder().decode(TaskRequest.self, from: body)
            self.updateTaskRequests.append((taskId, requestBody))
            throw URLError(.notConnectedToInternet)
        }
        return self
    }

    @discardableResult
    func enqueueDeleteSuccess() -> IntegrationMockServer {
        enqueueDeleteTask()
    }

    @discardableResult
    func enqueueDeleteTask() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "DELETE")
            let taskId = try self.taskId(from: request)
            self.deleteTaskIds.append(taskId)
            return (HTTPURLResponse(url: request.url!, statusCode: 204, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueDeleteTaskError(_ statusCode: Int) -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "DELETE")
            let taskId = try self.taskId(from: request)
            self.deleteTaskIds.append(taskId)
            return (HTTPURLResponse(url: request.url!, statusCode: statusCode, httpVersion: nil, headerFields: nil)!, Data())
        }
        return self
    }

    @discardableResult
    func enqueueDeleteNetworkFailure() -> IntegrationMockServer {
        responses.append { request in
            try self.assertMethod(request, "DELETE")
            let taskId = try self.taskId(from: request)
            self.deleteTaskIds.append(taskId)
            return (
                HTTPURLResponse(url: request.url!, statusCode: 503, httpVersion: nil, headerFields: nil)!,
                Data()
            )
        }
        return self
    }

    private func taskId(from request: URLRequest) throws -> String {
        guard let path = request.url?.path, path.hasPrefix("/v1/tasks/") else {
            throw APIError.invalidURL
        }
        return String(path.dropFirst("/v1/tasks/".count))
    }

    private func requestBody(from request: URLRequest) -> Data {
        if let body = request.httpBody {
            return body
        }
        guard let stream = request.httpBodyStream else {
            return Data()
        }
        stream.open()
        defer { stream.close() }
        var data = Data()
        let bufferSize = 1024
        let buffer = UnsafeMutablePointer<UInt8>.allocate(capacity: bufferSize)
        defer { buffer.deallocate() }
        while stream.hasBytesAvailable {
            let read = stream.read(buffer, maxLength: bufferSize)
            if read > 0 {
                data.append(buffer, count: read)
            }
        }
        return data
    }

    private func assertMethod(_ request: URLRequest, _ method: String) throws {
        guard request.httpMethod == method else {
            throw APIError.invalidResponse
        }
    }

    private func assertPath(_ request: URLRequest, _ path: String) throws {
        guard request.url?.path == path else {
            throw APIError.invalidURL
        }
    }

    private func assertTaskPath(_ request: URLRequest) throws {
        guard let path = request.url?.path,
              path.hasPrefix("/v1/tasks/"),
              !path.contains("/isValid/") else {
            throw APIError.invalidURL
        }
    }

    private func assertIsValidPath(_ request: URLRequest) throws {
        guard request.url?.path.contains("/isValid/") == true else {
            throw APIError.invalidURL
        }
    }
}
