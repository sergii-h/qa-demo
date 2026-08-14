import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
struct TaskFormScreenTests {
    @Test
    func shouldShowCreateModalTitleWhenCreateMode() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: {})
        )
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        // Then
        let title = try inspected.find(viewWithAccessibilityIdentifier: "modal-title")
        #expect(try title.text().string() == "New task")
        #expect(throws: Never.self) {
            _ = try inspected.find(viewWithAccessibilityIdentifier: "create-task-title-input")
        }
    }
    @Test
    func shouldDisableCreateButtonWhenTitleEmpty() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: {})
        )
        defer { ScreenTestSupport.expel() }
        // When
        let button = try view.inspect().find(viewWithAccessibilityIdentifier: "create-button").button()
        // Then
        #expect(button.isDisabled())
    }
    @Test
    func shouldShowSaveErrorWhenCreateRequestFails() async throws {
        // Given
        let api = MockTaskAPI()
        api.createHandler = { _ in throw APIErrorResponse(statusCode: 500, body: nil) }
        let repository = TaskRepository(api: api)
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: {}, viewModel: viewModel)
        )
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        try inspected.find(viewWithAccessibilityIdentifier: "create-task-title-input").textField().setInput("New title")
        try inspected.find(viewWithAccessibilityIdentifier: "create-button").button().tap()
        await AsyncTestSupport.waitUntil { viewModel.uiState.saveError != nil }
        // Then
        let error = try inspected.find(viewWithAccessibilityIdentifier: "save-error")
        #expect(try error.text().string() == "Request failed (500)")
    }
    @Test
    func shouldShowTitleErrorWhenTitleTooLongOnSubmit() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: {}, viewModel: viewModel)
        )
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        try inspected.find(viewWithAccessibilityIdentifier: "create-task-title-input").textField().setInput(String(repeating: "a", count: 101))
        try inspected.find(viewWithAccessibilityIdentifier: "create-button").button().tap()
        await AsyncTestSupport.waitUntil { viewModel.uiState.titleError != nil }
        // Then
        let error = try inspected.find(viewWithAccessibilityIdentifier: "title-error")
        #expect(try error.text().string() == "Title must not exceed 100 characters")
    }
    @Test
    func shouldShowEditModalTitleWhenEditModeAndTaskLoaded() async throws {
        // Given
        let api = MockTaskAPI()
        api.taskById[TaskFixtures.sampleTask.id] = TaskFixtures.sampleTask
        let repository = TaskRepository(api: api)
        let viewModel = TaskFormViewModel(
            repository: repository,
            mode: .edit,
            taskId: TaskFixtures.sampleTask.id,
            loadsTaskOnInit: false
        )
        ScreenTestSupport.resetLocaleToEnglish()

        // When
        await viewModel.loadTask(TaskFixtures.sampleTask.id)
        let view = ScreenTestSupport.host(
            TaskFormView(
                repository: repository,
                mode: .edit,
                taskId: TaskFixtures.sampleTask.id,
                onSaved: {},
                viewModel: viewModel
            )
        )
        defer { ScreenTestSupport.expel() }
        let inspected = try view.inspect()

        // Then
        let title = try inspected.find(viewWithAccessibilityIdentifier: "modal-title")
        #expect(try title.text().string() == "Edit task")
        let titleInput = try inspected.find(viewWithAccessibilityIdentifier: "edit-task-title-input")
        #expect(try titleInput.textField().input() == TaskFixtures.sampleTask.title)
    }
    @Test
    func shouldInvokeOnSavedWhenCreateSucceeds() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        var saved = false
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: { saved = true }, viewModel: viewModel)
        )
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        try inspected.find(viewWithAccessibilityIdentifier: "create-task-title-input").textField().setInput("Created task")
        try inspected.find(viewWithAccessibilityIdentifier: "create-button").button().tap()
        await AsyncTestSupport.waitUntil { saved }
        // Then
        #expect(saved)
    }
}
