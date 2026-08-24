import XCTest
import PactSwift
@testable import Demo

final class TasksGetAllPactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-get-all")

    func testShouldHaveGetAllTasksContractWhenListingTasks() async throws {
        TasksGetAllPactTests.mockService
            .uponReceiving("a request for all tasks")
            .given("tasks exist")
            .withRequest(method: .GET, path: "/v1/tasks")
            .willRespondWith(
                status: 200,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.taskListResponseBody()
            )

        try await TasksGetAllPactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.getTasks()
        }
    }
}
