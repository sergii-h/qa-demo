import XCTest
import PactSwift
@testable import Demo

final class TasksUpdateDuplicatePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-update")

    func testShouldHaveUpdateTaskDuplicateContractWhenUpdatingWithDuplicateTitle() async throws {
        let exampleMessage = "Task with title '\(PactFixtures.updateTaskRequest.title)' already exists"
        TasksUpdateDuplicatePactTests.mockService
            .uponReceiving("a task update request with duplicate title")
            .given("another task has the requested title")
            .withRequest(
                method: .PUT,
                path: PactFixtures.taskPathFromProviderState(),
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.updateTaskRequestBody()
            )
            .willRespondWith(
                status: 409,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.duplicateTitleErrorBody(exampleMessage: exampleMessage)
            )

        try await TasksUpdateDuplicatePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            do {
                _ = try await api.updateTask(id: PactFixtures.taskID, request: PactFixtures.updateTaskRequest)
                XCTFail("Expected duplicate title error")
            } catch let error as APIErrorResponse {
                XCTAssertEqual(error.statusCode, 409)
            }
        }
    }
}
