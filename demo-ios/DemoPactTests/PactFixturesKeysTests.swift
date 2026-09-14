import XCTest
@testable import Demo

final class PactFixturesKeysTests: XCTestCase {
    func testShouldMatchTaskCodableKeysWhenBuildingTaskResponseBody() throws {
        // Given
        let task = Task(
            id: PactFixtures.taskID,
            title: "Prepare release notes",
            description: "Document release tasks",
            status: .todo,
            priority: .medium,
            createdDate: "2026-04-26T09:00:00.000Z",
            updatedDate: "2026-04-26T09:00:00.000Z"
        )

        // When
        let encodedKeys = try jsonKeys(encoding: task)

        // Then
        XCTAssertEqual(encodedKeys, Set(PactFixtures.taskResponseBody().keys))
    }

    func testShouldMatchTaskRequestCodableKeysWhenBuildingCreateTaskRequestBody() throws {
        // Given / When
        let encodedKeys = try jsonKeys(encoding: PactFixtures.createTaskRequest)

        // Then
        XCTAssertEqual(encodedKeys, Set(PactFixtures.createTaskRequestBody().keys))
    }

    func testShouldMatchTaskRequestCodableKeysWhenBuildingUpdateTaskRequestBody() throws {
        // Given / When
        let encodedKeys = try jsonKeys(encoding: PactFixtures.updateTaskRequest)

        // Then
        XCTAssertEqual(encodedKeys, Set(PactFixtures.updateTaskRequestBody().keys))
    }

    func testShouldMatchErrorResponseCodableKeysWhenBuildingDuplicateTitleErrorBody() throws {
        // Given
        let error = ErrorResponse(message: "Task with title 'Prepare release notes' already exists")

        // When
        let encodedKeys = try jsonKeys(encoding: error)

        // Then
        XCTAssertEqual(
            encodedKeys,
            Set(PactFixtures.duplicateTitleErrorBody(exampleMessage: error.message ?? "").keys)
        )
    }

    private func jsonKeys<T: Encodable>(encoding value: T) throws -> Set<String> {
        let data = try JSONEncoder().encode(value)
        let object = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]
        return Set(object.keys)
    }
}
