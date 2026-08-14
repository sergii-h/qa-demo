import SwiftUI
import ViewInspector
import Testing
@testable import Demo

@MainActor
struct TaskListScreenTests {
    @Test
    func shouldRenderEmptyStateWhenNoTasksLoaded() async throws {
        // Given
        let api = MockTaskAPI()
        api.tasks = []
        let repository = TaskRepository(api: api)
        let viewModel = TaskListViewModel(repository: repository, loadsTasksOnInit: false)
        await viewModel.loadTasks()

        // When
        let view = TaskListView(
            repository: repository,
            refreshTrigger: 0,
            onCreateTask: {},
            onEditTask: { _ in },
            onTaskInfo: { _ in },
            viewModel: viewModel
        )
        .environment(AppLocale.shared)
        let inspected = try view.inspect()

        // Then
        #expect(throws: Never.self) {
            _ = try inspected.find(viewWithAccessibilityIdentifier: "empty-tasks")
        }
    }
}
