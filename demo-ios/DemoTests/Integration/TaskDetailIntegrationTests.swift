import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
final class TaskDetailIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldOpenInfoFormAndDisplayTaskDetailsForAllValuesDataset() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try harness.assertTextEquals("description", context.description ?? "")
        try await harness.assertIsDisplayed("status-tag-\(context.status.rawValue)")
        try await harness.assertIsDisplayed("priority-tag-\(context.priority.rawValue)")
        try await harness.assertIsDisplayed("valid")
    }
    @Test
    func shouldOpenInfoFormAndDisplayTaskDetailsForRequiredOnlyValuesDataset() async throws {
        // Given
        let context = TaskTestContext(description: nil)
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try harness.assertTextEquals("description", "No description")
        try await harness.assertIsDisplayed("status-tag-\(context.status.rawValue)")
        try await harness.assertIsDisplayed("priority-tag-\(context.priority.rawValue)")
        try await harness.assertIsDisplayed("valid")
    }
    @Test
    func shouldCloseInfoFormOnCloseAction() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openDetailFromList(taskId: context.id)
        // When
        try await harness.tap("close-button")
        // Then
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldHaveTranslationsForDetailView() async throws {
        // Given
        let context = TaskTestContext(status: .todo, priority: .low)
        harness.mockServer
            .enqueueGetTasksForLanguageSwitch([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
        AppLocale.shared.setLanguage(AppLocale.spanish)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try harness.assertTextEquals("detail-description-label", "Descripción")
        try harness.assertTextEquals("detail-validated-label", "Validado:")
        try harness.assertTextEquals("status-tag-\(context.status.rawValue)", "Por hacer")
        try harness.assertTextEquals("priority-tag-\(context.priority.rawValue)", "Baja")
    }
    @Test(arguments: GetTaskFailure.allCases)
    func shouldNotOpenInfoFormWhenTaskDetailsRequestFails(failure: GetTaskFailure) async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer.enqueueGetTasks([context.createTaskResponse()])
        failure.enqueue(harness.mockServer)
        harness.mockServer.enqueueIsValid(false)
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.tap("info-button-\(context.id)")
        try await harness.waitForDetailScreenToSettle()
        try await harness.waitForDetailLoadError()
        // Then
        try harness.assertTextEquals("load-error", failure.expectedLoadError)
        try harness.assertIsNotDisplayed("description")
    }
}
@MainActor
@Suite(.serialized)
final class ExternalValidationIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldDisplayValidatedStateWhenExternalValidationReturnsTrue() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(true)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try await harness.assertIsDisplayed("valid")
        try harness.assertIsNotDisplayed("notValid")
    }
    @Test
    func shouldDisplayNotValidatedStateWhenExternalValidationReturnsFalse() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueIsValid(false)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try await harness.assertIsDisplayed("notValid")
        try harness.assertIsNotDisplayed("valid")
    }
    @Test(arguments: IsValidFailure.allCases)
    func shouldShowInvalidValidationSignWhenValidationRequestFails(failure: IsValidFailure) async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
        failure.enqueue(harness.mockServer)
        harness.launchApp(initialPath: [.detail(context.id)])
        try await harness.openDetail(taskId: context.id)
        // Then
        try harness.assertTextEquals("description", context.description ?? "")
        try await harness.assertIsDisplayed("status-tag-\(context.status.rawValue)")
        try await harness.assertIsDisplayed("priority-tag-\(context.priority.rawValue)")
        try await harness.assertIsDisplayed("notValid")
        try harness.assertIsNotDisplayed("load-error")
    }
}
