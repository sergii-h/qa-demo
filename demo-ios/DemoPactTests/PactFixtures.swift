import Foundation
import PactSwift
@testable import Demo

enum PactFixtures {
    static let consumer = "demo-ios"
    static let pactsDirectory: URL = {
        if let configured = ProcessInfo.processInfo.environment["PACT_OUTPUT_DIR"] {
            let url = URL(fileURLWithPath: configured, isDirectory: true)
            try? FileManager.default.createDirectory(at: url, withIntermediateDirectories: true)
            return url
        }
        let url = FileManager.default.temporaryDirectory.appendingPathComponent("pacts", isDirectory: true)
        try? FileManager.default.createDirectory(at: url, withIntermediateDirectories: true)
        return url
    }()

    static func mockService(provider: String) -> MockService {
        MockService(
            consumer: consumer,
            provider: provider,
            writePactTo: pactsDirectory
        )
    }
    static let taskID = "507f1f77bcf86cd799439011"
    static let timestampPattern = #"^\d{4}-\d{2}-\d{2}T.*$"#
    static let emptyProviderState = ProviderState(description: "", params: [:])

    static func makeTaskAPI(baseURL: String) -> TaskAPI {
        APIClient.makeTaskAPI(baseURL: apiBaseURL(from: baseURL))
    }

    static func apiBaseURL(from baseURL: String) -> URL {
        URL(string: "\(baseURL)/v1/")!
    }

    static let createTaskRequest = TaskRequest(
        title: "Prepare release notes",
        description: "Document release tasks",
        status: .todo,
        priority: .medium
    )

    static let updateTaskRequest = TaskRequest(
        title: "Prepare release notes - updated",
        description: "Document release tasks in detail",
        status: .inProgress,
        priority: .high
    )

    static func taskResponseBody(titleExample: String = "Prepare release notes") -> [String: Any] {
        [
            "id": Matcher.SomethingLike(taskID),
            "title": Matcher.SomethingLike(titleExample),
            "description": Matcher.SomethingLike("Document release tasks"),
            "status": Matcher.SomethingLike(TaskStatus.todo.rawValue),
            "priority": Matcher.SomethingLike(TaskPriority.medium.rawValue),
            "createdDate": Matcher.RegexLike(value: "2026-04-26T09:00:00.000Z", pattern: timestampPattern),
            "updatedDate": Matcher.RegexLike(value: "2026-04-26T09:00:00.000Z", pattern: timestampPattern),
        ]
    }

    static func createTaskRequestBody() -> [String: Any] {
        [
            "title": Matcher.SomethingLike(createTaskRequest.title),
            "description": Matcher.SomethingLike(createTaskRequest.description!),
            "status": Matcher.SomethingLike(createTaskRequest.status.rawValue),
            "priority": Matcher.SomethingLike(createTaskRequest.priority.rawValue),
        ]
    }

    static func updateTaskRequestBody() -> [String: Any] {
        [
            "title": Matcher.SomethingLike(updateTaskRequest.title),
            "description": Matcher.SomethingLike(updateTaskRequest.description!),
            "status": Matcher.SomethingLike(updateTaskRequest.status.rawValue),
            "priority": Matcher.SomethingLike(updateTaskRequest.priority.rawValue),
        ]
    }

    static func taskListResponseBody() -> Matcher.EachLike {
        Matcher.EachLike(taskResponseBody())
    }

    static func duplicateTitleErrorBody(exampleMessage: String) -> [String: Any] {
        [
            "message": Matcher.RegexLike(
                value: exampleMessage,
                pattern: "^Task with title '.*' already exists$"
            ),
        ]
    }
}
