import Foundation
import Testing
@testable import Demo

@MainActor
struct TaskDetailViewModelTests {
    @Test
    func shouldLoadTaskAndValidationWhenInitialized() async {
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

        // When
        await viewModel.load()

        // Then
        #expect(viewModel.uiState.task == TaskFixtures.sampleTask)
        #expect(viewModel.uiState.isValid)
        #expect(viewModel.uiState.isLoading == false)
    }

    @Test
    func shouldSetErrorMessageWhenTaskLoadFails() async {
        // Given
        let api = MockTaskAPI()
        api.getTaskError = APIErrorResponse(statusCode: 404, body: nil)
        let repository = TaskRepository(api: api)
        let viewModel = TaskDetailViewModel(repository: repository, taskId: "missing", loadsOnInit: false)

        // When
        await viewModel.load()

        // Then
        #expect(viewModel.uiState.task == nil)
        #expect(viewModel.uiState.errorMessage == "Task not found")
    }
}
