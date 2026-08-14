import Foundation
import Testing
@testable import Demo

struct TaskRepositoryTests {
    @Test
    func shouldDelegateGetTasksToAPI() async throws {
        // Given
        let api = MockTaskAPI()
        api.tasks = [TaskFixtures.sampleTask]
        let repository = TaskRepository(api: api)

        // When
        let tasks = try await repository.getTasks()

        // Then
        #expect(tasks == [TaskFixtures.sampleTask])
    }
}

struct ErrorMessagesTests {
    @Test @MainActor
    func shouldMap409ToDuplicateTitleMessage() {
        let locale = AppLocale.shared
        let message = ErrorMessages.mapTaskError(APIErrorResponse(statusCode: 409, body: nil), locale: locale)
        #expect(message == "Task with this title already exists")
    }

    @Test
    func shouldDetectDuplicateTitleErrorFor409() {
        #expect(ErrorMessages.isDuplicateTitleError(APIErrorResponse(statusCode: 409, body: nil)))
        #expect(!ErrorMessages.isDuplicateTitleError(APIErrorResponse(statusCode: 500, body: nil)))
    }
}

struct URLSessionTaskAPITests {
    @Test
    func shouldDecodeTasksFromStubbedResponse() async throws {
        // Given
        let task = TaskFixtures.sampleTask
        let data = try JSONEncoder().encode([task])
        StubURLProtocol.requestHandler = { request in
            let response = HTTPURLResponse(url: request.url!, statusCode: 200, httpVersion: nil, headerFields: nil)!
            return (response, data)
        }
        defer { StubURLProtocol.requestHandler = nil }
        let api = APIClient.makeTaskAPI(
            baseURL: URL(string: "http://stub.test/v1/")!,
            session: StubURLSessionFactory.makeSession()
        )

        // When
        let tasks = try await api.getTasks()

        // Then
        #expect(tasks == [task])
    }
}
