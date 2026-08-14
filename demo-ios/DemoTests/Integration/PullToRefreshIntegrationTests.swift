import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
final class PullToRefreshIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldShowNewTaskWhenPullToRefreshReturnsUpdatedList() async throws {
        // Given
        let firstContext = TaskTestContext()
        let secondContext = TaskTestContext()
        harness.mockServer.enqueueGetTasks([firstContext.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.assertIsDisplayed("task-title-\(firstContext.id)")
        harness.mockServer.enqueueGetTasks([
            firstContext.createTaskResponse(),
            secondContext.createTaskResponse(),
        ])
        // When
        try await harness.pullToRefresh()
        // Then
        try await harness.assertIsDisplayed("task-title-\(firstContext.id)")
        try await harness.assertIsDisplayed("task-title-\(secondContext.id)")
    }
    @Test
    func shouldKeepExistingTasksWhenPullToRefreshReturnsSameList() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer.enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.assertIsDisplayed("task-title-\(context.id)")
        harness.mockServer.enqueueGetTasks([context.createTaskResponse()])
        // When
        try await harness.pullToRefresh()
        // Then
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldKeepEmptyListWhenPullToRefreshReturnsEmptyList() async throws {
        // Given
        harness.mockServer.enqueueGetTasks()
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.assertIsDisplayed("empty-tasks")
        harness.mockServer.enqueueGetTasks()
        // When
        try await harness.pullToRefresh()
        // Then
        try await harness.assertIsDisplayed("empty-tasks")
        try await harness.assertIsDisplayed("add-task-button")
    }
    @Test
    func shouldKeepExistingTasksWhenPullToRefreshFailsWithServerError() async throws {
        // Given
        let firstContext = TaskTestContext()
        let secondContext = TaskTestContext()
        harness.mockServer.enqueueGetTasks([
            firstContext.createTaskResponse(),
            secondContext.createTaskResponse(),
        ])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.assertIsDisplayed("task-title-\(firstContext.id)")
        try await harness.assertIsDisplayed("task-title-\(secondContext.id)")
        harness.mockServer.enqueueGetTasksError(500)
        // When
        try await harness.pullToRefresh()
        // Then
        try await harness.assertIsDisplayed("task-title-\(firstContext.id)")
        try await harness.assertIsDisplayed("task-title-\(secondContext.id)")
        try await harness.assertIsDisplayed("error-snackbar")
    }
}
