import XCTest
import PactSwift
@testable import Demo

final class TasksCreatePactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-create")

    func testShouldHaveCreateTaskContractWhenPostingValidTask() async throws {
        TasksCreatePactTests.mockService
            .uponReceiving("a valid task creation request")
            .given("task title is unique")
            .withRequest(
                method: .POST,
                path: "/v1/tasks",
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.createTaskRequestBody()
            )
            .willRespondWith(
                status: 201,
                headers: ["Content-Type": "application/json"],
                body: PactFixtures.taskResponseBody()
            )

        try await TasksCreatePactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.createTask(PactFixtures.createTaskRequest)
        }
    }
}
