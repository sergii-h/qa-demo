import XCTest
import PactSwift
@testable import Demo

final class TasksGetByIdPactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-get-by-id")

    func testShouldHaveGetTaskByIdContractWhenFetchingTask() async throws {
        TasksGetByIdPactTests.mockService
            .uponReceiving("a request for a task by id")
            .given("a task exists")
            .withRequest(method: .GET, path: PactFixtures.taskPathFromProviderState())
            .willRespondWith(
                status: 200,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.taskResponseBody()
            )

        try await TasksGetByIdPactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.getTask(id: PactFixtures.taskID)
        }
    }
}
