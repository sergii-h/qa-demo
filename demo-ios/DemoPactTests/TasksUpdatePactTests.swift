import XCTest
import PactSwift
@testable import Demo

final class TasksUpdatePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-update")

    func testShouldHaveUpdateTaskContractWhenPuttingValidTask() async throws {
        TasksUpdatePactTests.mockService
            .uponReceiving("a valid task update request")
            .given(ProviderState(description: "task exists and title is unique", params: [:]))
            .withRequest(
                method: .PUT,
                path: "/v1/tasks/\(PactFixtures.taskID)",
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.updateTaskRequestBody()
            )
            .willRespondWith(
                status: 200,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.taskResponseBody(titleExample: "Prepare release notes - updated")
            )

        try await TasksUpdatePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.updateTask(id: PactFixtures.taskID, request: PactFixtures.updateTaskRequest)
        }
    }
}
