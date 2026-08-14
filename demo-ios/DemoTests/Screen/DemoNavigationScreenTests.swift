import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
struct DemoNavigationScreenTests {
    @Test
    func shouldRenderTaskListWhenNavigationStarts() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(DemoNavigation(repository: repository))
        defer { ScreenTestSupport.expel() }
        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(TaskListView.self)
        }
    }
    @Test
    func shouldRenderCreateDestinationWhenCreateRouteHosted() async throws {
        // Given
        let repository = TaskRepository(api: MockTaskAPI())
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(repository: repository, mode: .create, taskId: nil, onSaved: {})
        )
        defer { ScreenTestSupport.expel() }
        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(viewWithAccessibilityIdentifier: "create-task-title-input")
        }
    }
    @Test
    func shouldRenderEditDestinationWhenEditRouteHosted() async throws {
        // Given
        let api = MockTaskAPI()
        api.taskById[TaskFixtures.sampleTask.id] = TaskFixtures.sampleTask
        let repository = TaskRepository(api: api)
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(
            TaskFormView(
                repository: repository,
                mode: .edit,
                taskId: TaskFixtures.sampleTask.id,
                onSaved: {}
            )
        )
        defer { ScreenTestSupport.expel() }
        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(viewWithAccessibilityIdentifier: "edit-task-title-input")
        }
    }
    @Test
    func shouldRenderDetailDestinationWhenDetailRouteHosted() async throws {
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

        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(viewWithAccessibilityIdentifier: "description")
        }
    }
}
