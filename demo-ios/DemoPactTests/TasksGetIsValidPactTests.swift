import XCTest
import PactSwift
@testable import Demo

final class TasksGetIsValidPactTests: XCTestCase {
    static let mockService = PactFixtures.mockService(provider: "demo-service-tasks-get-is-valid")

    func testShouldHaveIsValidContractWhenCheckingTask() async throws {
        TasksGetIsValidPactTests.mockService
            .uponReceiving("a request for task validation status")
            .given("validation result is true for the task")
            .withRequest(method: .GET, path: PactFixtures.isValidPathFromProviderState())
            .willRespondWith(
                status: 200,
                headers: ["Content-Type": "application/json"],
                body: Matcher.SomethingLike(true)
            )

        try await TasksGetIsValidPactTests.mockService.run(timeout: 5) { baseURL in
            let api = PactFixtures.makeTaskAPI(baseURL: baseURL)
            _ = try await api.isValid(id: PactFixtures.taskID)
        }
    }
}
