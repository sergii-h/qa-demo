import Foundation
import Testing
@testable import Demo

@MainActor
struct TaskListViewModelTests {
    @Test
    func shouldLoadTasksWhenInitialized() async {
        // Given
        let api = MockTaskAPI()
        api.tasks = [TaskFixtures.sampleTask]
        let repository = TaskRepository(api: api)

        // When
        let viewModel = TaskListViewModel(repository: repository)
        await AsyncTestSupport.waitUntil { !viewModel.uiState.isLoading }

        // Then
        #expect(viewModel.uiState.tasks == [TaskFixtures.sampleTask])
        #expect(viewModel.uiState.isLoading == false)
    }

    @Test
    func shouldSetErrorMessageWhenLoadFails() async {
        // Given
        let api = MockTaskAPI()
        api.getTasksError = APIErrorResponse(statusCode: 500, body: nil)
        let repository = TaskRepository(api: api)

        // When
        let viewModel = TaskListViewModel(repository: repository)
        await AsyncTestSupport.waitUntil { viewModel.uiState.errorMessage != nil }

        // Then
        #expect(viewModel.uiState.tasks.isEmpty)
        #expect(viewModel.uiState.errorMessage == "Request failed (500)")
    }

    @Test
    func shouldRemoveTaskWhenDeleteSucceeds() async {
        // Given
        let api = MockTaskAPI()
        api.tasks = [TaskFixtures.sampleTask]
        api.taskById[TaskFixtures.sampleTask.id] = TaskFixtures.sampleTask
        let repository = TaskRepository(api: api)
        let viewModel = TaskListViewModel(repository: repository, loadsTasksOnInit: false)
        await viewModel.loadTasks()

        // When
        await viewModel.deleteTask(TaskFixtures.sampleTask)

        // Then
        #expect(viewModel.uiState.tasks.isEmpty)
    }
}
