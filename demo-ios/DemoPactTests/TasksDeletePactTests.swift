import XCTest
import PactSwift
@testable import Demo

final class TasksDeletePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-delete")

    func testShouldHaveDeleteTaskContractWhenDeletingTask() async throws {
        TasksDeletePactTests.mockService
            .uponReceiving("a request to delete a task")
            .given("a task exists to delete")
            .withRequest(method: .DELETE, path: PactFixtures.taskPathFromProviderState())
            .willRespondWith(status: 204)

        try await TasksDeletePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            try await api.deleteTask(id: PactFixtures.taskID)
        }
    }
}
