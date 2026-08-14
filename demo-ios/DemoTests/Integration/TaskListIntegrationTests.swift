import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
final class TaskListIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldRenderTaskListWithFetchedDataWhenListIsFirstShown() async throws {
        // Given
        let firstContext = TaskTestContext(status: .todo, priority: .low)
        let secondContext = TaskTestContext(status: .done, priority: .high)
        harness.mockServer.enqueueGetTasks([
            firstContext.createTaskResponse(),
            secondContext.createTaskResponse(),
        ])
        harness.launchApp()
        try await harness.waitUntilReady()
        // Then
        #expect(harness.mockServer.getTasksRequestCount == 1)
        try await harness.assertIsDisplayed("task-title-\(firstContext.id)")
        try await harness.assertIsDisplayed("task-title-\(secondContext.id)")
        try harness.assertTextEquals("task-title-\(firstContext.id)", firstContext.title)
        try harness.assertTextEquals("task-title-\(secondContext.id)", secondContext.title)
    }
    @Test
    func shouldDisplayStatusPriorityTagsAndActionButtonsForEachTask() async throws {
        // Given
        let firstContext = TaskTestContext(status: .todo, priority: .low)
        let secondContext = TaskTestContext(status: .inProgress, priority: .medium)
        let thirdContext = TaskTestContext(status: .done, priority: .high)
        harness.mockServer.enqueueGetTasks([
            firstContext.createTaskResponse(),
            secondContext.createTaskResponse(),
            thirdContext.createTaskResponse(),
        ])
        harness.launchApp()
        try await harness.waitUntilReady()
        // Then
        try await harness.assertIsDisplayed("status-tag-\(firstContext.status.rawValue)")
        try await harness.assertIsDisplayed("priority-tag-\(firstContext.priority.rawValue)")
        try await harness.assertIsDisplayed("info-button-\(firstContext.id)")
        try await harness.assertIsDisplayed("edit-button-\(firstContext.id)")
        try await harness.assertIsDisplayed("delete-button-\(firstContext.id)")
        try await harness.assertIsDisplayed("info-button-\(secondContext.id)")
        try await harness.assertIsDisplayed("edit-button-\(secondContext.id)")
        try await harness.assertIsDisplayed("delete-button-\(secondContext.id)")
        try await harness.assertIsDisplayed("info-button-\(thirdContext.id)")
        try await harness.assertIsDisplayed("edit-button-\(thirdContext.id)")
        try await harness.assertIsDisplayed("delete-button-\(thirdContext.id)")
    }
    @Test
    func shouldRenderEmptyListStateWhenTasksResponseIsEmpty() async throws {
        // Given
        harness.mockServer.enqueueGetTasks()
        harness.launchApp()
        try await harness.waitUntilReady()
        // Then
        try await harness.assertIsDisplayed("empty-tasks")
        try await harness.assertIsDisplayed("add-task-button")
    }
    @Test
    func shouldOpenCreateTaskFormFromListActions() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer.enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.openCreateFormFromList()
        // Then
        try await harness.assertIsDisplayed("create-task-title-input")
    }
    @Test
    func shouldOpenTaskInfoFormFromListActions() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.openDetailFromList(taskId: context.id)
        // Then
        try await harness.assertIsDisplayed("description")
    }
    @Test
    func shouldOpenTaskEditFormFromListActions() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.openEditFormFromList(taskId: context.id)
        // Then
        try await harness.assertIsDisplayed("edit-task-title-input")
    }
    @Test
    func shouldHaveTranslationsForTaskList() async throws {
        // Given
        let context = TaskTestContext(status: .todo, priority: .low)
        harness.mockServer.enqueueGetTasksForLanguageSwitch([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        AppLocale.shared.setLanguage(AppLocale.spanish)
        try await harness.applyCurrentLocale()
        // Then
        try harness.assertTextEquals("page-title", "Tareas")
        try harness.assertTextEquals("status-tag-\(context.status.rawValue)", "Por hacer")
        try harness.assertTextEquals("priority-tag-\(context.priority.rawValue)", "Baja")
    }
    @Test(arguments: GetTasksFailure.allCases)
    func shouldKeepCreateFlowAvailableWhenInitialGetTasksRequestFails(failure: GetTasksFailure) async throws {
        // Given
        let context = TaskTestContext()
        failure.enqueue(harness.mockServer)
        harness.launchApp()
        try await harness.waitUntilReady()
        // Then
        try await harness.assertIsDisplayed("empty-tasks")
        try await harness.assertIsDisplayed("add-task-button")
        // When
        try await harness.openCreateFormFromList()
        // Then
        try await harness.assertIsDisplayed("create-task-title-input")
    }
}
@MainActor
@Suite(.serialized)
final class DeleteTaskIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldSendDeleteRequestAndRemoveTaskFromListAfterSuccessfulDelete() async throws {
        // Given
        let deleteContext = TaskTestContext()
        let keepContext = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([
                deleteContext.createTaskResponse(),
                keepContext.createTaskResponse(),
            ])
            .enqueueDeleteSuccess()
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.tap("delete-button-\(deleteContext.id)")
        try await harness.waitUntilCondition { harness.mockServer.deleteTaskIds.contains(deleteContext.id) }
        // Then
        #expect(harness.mockServer.deleteTaskIds == [deleteContext.id])
        try await harness.assertIsDisplayed("task-title-\(keepContext.id)")
        try harness.assertIsNotDisplayed("task-title-\(deleteContext.id)")
        #expect(harness.mockServer.getTasksRequestCount == 1)
    }
    @Test
    func shouldAllowDeleteRetryAfterFailureAndRemoveTaskWhenRetrySucceeds() async throws {
        // Given
        let deleteContext = TaskTestContext()
        let keepContext = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([
                deleteContext.createTaskResponse(),
                keepContext.createTaskResponse(),
            ])
            .enqueueDeleteTaskError(500)
            .enqueueDeleteSuccess()
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.tap("delete-button-\(deleteContext.id)")
        try await harness.waitUntilCondition { harness.mockServer.deleteTaskIds.count == 1 }
        try await harness.assertIsDisplayed("error-snackbar")
        // And
        try await harness.tap("delete-button-\(deleteContext.id)")
        try await harness.waitUntilCondition { harness.mockServer.deleteTaskIds.count == 2 }
        // Then
        try await harness.assertIsDisplayed("task-title-\(keepContext.id)")
        try harness.assertIsNotDisplayed("task-title-\(deleteContext.id)")
    }
    @Test(arguments: DeleteFailure.allCases)
    func shouldKeepTaskInListWhenDeleteFails(failure: DeleteFailure) async throws {
        // Given
        let deleteContext = TaskTestContext()
        let keepContext = TaskTestContext()
        harness.mockServer.enqueueGetTasks([
            deleteContext.createTaskResponse(),
            keepContext.createTaskResponse(),
        ])
        failure.enqueue(harness.mockServer)
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try await harness.tap("delete-button-\(deleteContext.id)")
        try await harness.waitUntilCondition { harness.mockServer.deleteTaskIds.contains(deleteContext.id) }
        // Then
        #expect(harness.mockServer.deleteTaskIds == [deleteContext.id])
        try await harness.assertIsDisplayed("task-title-\(deleteContext.id)")
        try await harness.assertIsDisplayed("task-title-\(keepContext.id)")
        try await harness.assertIsDisplayed("error-snackbar")
    }
}
