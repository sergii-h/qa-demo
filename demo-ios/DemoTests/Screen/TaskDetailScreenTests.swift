import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
struct TaskDetailScreenTests {
    @Test
    func shouldShowTaskDetailsWhenLoadSucceeds() async throws {
        // Given
        let api = MockTaskAPI()
        api.taskById[TaskFixtures.sampleTask.id] = TaskFixtures.sampleTask
        api.validById[TaskFixtures.sampleTask.id] = true
        let repository = TaskRepository(api: api)
        let viewModel = TaskDetailViewModel(
            repository: repository,
            taskId: TaskFixtures.sampleTask.id,
            loadsOnInit: false
        )
        ScreenTestSupport.resetLocaleToEnglish()

        // When
        await viewModel.load()
        let view = ScreenTestSupport.host(
            TaskDetailView(repository: repository, taskId: TaskFixtures.sampleTask.id, viewModel: viewModel)
        )
        defer { ScreenTestSupport.expel() }
        let inspected = try view.inspect()

        // Then
        let description = try inspected.find(viewWithAccessibilityIdentifier: "description")
        #expect(try description.text().string() == TaskFixtures.sampleTask.description)
        #expect(throws: Never.self) {
            _ = try inspected.find(viewWithAccessibilityIdentifier: "valid")
        }
        let modalTitle = try inspected.find(viewWithAccessibilityIdentifier: "modal-title")
        #expect(try modalTitle.text().string() == TaskFixtures.sampleTask.title)
    }
    @Test
    func shouldShowNoDescriptionWhenTaskHasNoDescription() async throws {
        // Given
        let task = Task(
            id: "task-no-desc",
            title: "Title only",
            description: nil,
            status: .todo,
            priority: .medium,
            createdDate: nil,
            updatedDate: nil
        )
        let api = MockTaskAPI()
        api.taskById[task.id] = task
        api.validById[task.id] = false
        let repository = TaskRepository(api: api)
        let viewModel = TaskDetailViewModel(repository: repository, taskId: task.id, loadsOnInit: false)
        ScreenTestSupport.resetLocaleToEnglish()

        // When
        await viewModel.load()
        let view = ScreenTestSupport.host(TaskDetailView(repository: repository, taskId: task.id, viewModel: viewModel))
        defer { ScreenTestSupport.expel() }
        let inspected = try view.inspect()

        // Then
        let description = try inspected.find(viewWithAccessibilityIdentifier: "description")
        #expect(try description.text().string() == "No description")
        #expect(throws: Never.self) {
            _ = try inspected.find(viewWithAccessibilityIdentifier: "notValid")
        }
    }
    @Test
    func shouldShowLoadErrorWhenTaskLoadFails() async throws {
        // Given
        let api = MockTaskAPI()
        api.getTaskError = APIErrorResponse(statusCode: 404, body: nil)
        let repository = TaskRepository(api: api)
        let viewModel = TaskDetailViewModel(repository: repository, taskId: "missing", loadsOnInit: false)
        ScreenTestSupport.resetLocaleToEnglish()

        // When
        await viewModel.load()
        let view = ScreenTestSupport.host(TaskDetailView(repository: repository, taskId: "missing", viewModel: viewModel))
        defer { ScreenTestSupport.expel() }
        let inspected = try view.inspect()

        // Then
        let error = try inspected.find(viewWithAccessibilityIdentifier: "load-error")
        #expect(try error.text().string() == "Task not found")
    }
    @Test
    func shouldShowCreatedAndUpdatedDatesWhenPresent() async throws {
        // Given
        let api = MockTaskAPI()
        api.taskById[TaskFixtures.sampleTask.id] = TaskFixtures.sampleTask
        api.validById[TaskFixtures.sampleTask.id] = true
        let repository = TaskRepository(api: api)
        let viewModel = TaskDetailViewModel(
            repository: repository,
            taskId: TaskFixtures.sampleTask.id,
            loadsOnInit: false
        )
        ScreenTestSupport.resetLocaleToEnglish()

        // When
        await viewModel.load()
        let view = ScreenTestSupport.host(
            TaskDetailView(repository: repository, taskId: TaskFixtures.sampleTask.id, viewModel: viewModel)
        )
        defer { ScreenTestSupport.expel() }
        let inspected = try view.inspect()

        // Then
        let expectedCreated = TaskDateFormatter.format(TaskFixtures.sampleTask.createdDate!, locale: AppLocale.shared)
        let expectedUpdated = TaskDateFormatter.format(TaskFixtures.sampleTask.updatedDate!, locale: AppLocale.shared)
        let created = try inspected.find(viewWithAccessibilityIdentifier: "created-date")
        #expect(try created.text().string() == expectedCreated)
        let updated = try inspected.find(viewWithAccessibilityIdentifier: "updated-date")
        #expect(try updated.text().string() == expectedUpdated)
    }
}
