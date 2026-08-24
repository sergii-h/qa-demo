import XCTest
import PactSwift
@testable import Demo

final class TasksUpdatePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-update")

    func testShouldHaveUpdateTaskContractWhenPuttingValidTask() async throws {
        TasksUpdatePactTests.mockService
            .uponReceiving("a valid task update request")
            .given("a task exists to update and title is unique")
            .withRequest(
                method: .PUT,
                path: PactFixtures.taskPathFromProviderState(),
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.updateTaskRequestBody()
            )
            .willRespondWith(
                status: 200,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.taskResponseBody(titleExample: PactFixtures.updateTaskRequest.title)
            )

        try await TasksUpdatePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.updateTask(id: PactFixtures.taskID, request: PactFixtures.updateTaskRequest)
        }
    }
}
