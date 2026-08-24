import XCTest
import PactSwift
@testable import Demo

final class TasksCreateDuplicatePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-create")

    func testShouldHaveDuplicateTitleContractWhenPostingConflictingTask() async throws {
        let exampleMessage = "Task with title 'Prepare release notes' already exists"
        TasksCreateDuplicatePactTests.mockService
            .uponReceiving("a task creation request with duplicate title")
            .given("task title already exists")
            .withRequest(
                method: .POST,
                path: "/v1/tasks",
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.createTaskRequestBody()
            )
            .willRespondWith(
                status: 409,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.duplicateTitleErrorBody(exampleMessage: exampleMessage)
            )

        try await TasksCreateDuplicatePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            do {
                _ = try await api.createTask(PactFixtures.createTaskRequest)
                XCTFail("Expected duplicate title error")
            } catch let error as APIErrorResponse {
                XCTAssertEqual(error.statusCode, 409)
            }
        }
    }
}
