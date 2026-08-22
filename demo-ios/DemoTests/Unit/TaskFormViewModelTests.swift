import Foundation
import Testing
@testable import Demo

@MainActor
struct TaskFormViewModelTests {
    @Test
    func shouldSetTitleErrorWhenTitleTooLong() async {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        viewModel.onTitleChange(String(repeating: "a", count: 101))

        // When
        await viewModel.save()

        // Then
        #expect(viewModel.uiState.titleError == "Title must not exceed 100 characters")
    }

    @Test
    func shouldSetTitleErrorWhenDuplicateTitleReturned() async {
        // Given
        let api = MockTaskAPI()
        api.createHandler = { _ in throw APIErrorResponse(statusCode: 409, body: nil) }
        let repository = TaskRepository(api: api)
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        viewModel.onTitleChange("Unique title")

        // When
        await viewModel.save()

        // Then
        #expect(viewModel.uiState.titleError == "Task with this title already exists")
    }

    @Test
    func shouldSetSaveErrorWhenNonConflictErrorReturned() async {
        // Given
        let api = MockTaskAPI()
        api.createHandler = { _ in throw APIErrorResponse(statusCode: 500, body: nil) }
        let repository = TaskRepository(api: api)
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        viewModel.onTitleChange("Unique title")

        // When
        await viewModel.save()

        // Then
        #expect(viewModel.uiState.saveError == "Request failed (500)")
    }

    @Test
    func shouldMarkSaveSucceededWhenCreateSucceeds() async {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        let viewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        viewModel.onTitleChange("New task")

        // When
        await viewModel.save()

        // Then
        #expect(viewModel.uiState.saveSucceeded)
    }

    @Test
    func shouldPrefillFieldsWhenEditModeInitialized() async {
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

        // When
        await viewModel.loadTask(TaskFixtures.sampleTask.id)

        // Then
        #expect(viewModel.uiState.title == TaskFixtures.sampleTask.title)
        #expect(viewModel.uiState.description == TaskFixtures.sampleTask.description)
        #expect(viewModel.uiState.status == TaskFixtures.sampleTask.status)
        #expect(viewModel.uiState.priority == TaskFixtures.sampleTask.priority)
    }
}
