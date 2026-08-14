import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@Suite(.serialized)
@MainActor
final class CreateTaskIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldCreateTaskWithAllValuesSendCorrectPostRequestAndAddNewTaskToList() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        try await harness.setText("task-description-input", value: context.description ?? "")
        try harness.selectStatus(context.status)
        try harness.selectPriority(context.priority)
        // When
        try await harness.submitCreateForm()
        // Then
        #expect(harness.mockServer.createTaskRequests == [context.createTaskRequest()])
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldCreateTaskWithRequiredValuesSendCorrectPostRequestAndAddNewTaskToList() async throws {
        // Given
        let context = TaskTestContext(description: nil, createdDate: nil, updatedDate: nil)
        harness.mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        // When
        try await harness.submitCreateForm()
        // Then
        #expect(harness.mockServer.createTaskRequests == [context.createTaskRequest()])
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldAllowSuccessfulCreationAfterInvalidTitleIsCorrected() async throws {
        // Given
        let context = TaskTestContext(description: nil)
        harness.mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: String(repeating: "a", count: 101))
        // When
        try await harness.clickSubmitCreateForm()
        // Then
        try harness.assertTextEquals("title-error", "Title must not exceed 100 characters")
        #expect(harness.mockServer.createTaskRequests.isEmpty)
        // When
        try await harness.clearText("create-task-title-input")
        try await harness.setText("create-task-title-input", value: context.title)
        try await harness.submitCreateForm()
        // Then
        #expect(harness.mockServer.createTaskRequests == [context.createTaskRequest()])
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldNotCreateTaskWhenCreateFormIsClosedWithoutSavingAndResetFormOnReopen() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer.enqueueGetTasks().enqueueGetTasks()
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        try await harness.setText("task-description-input", value: context.description ?? "")
        // When
        try await harness.tap("close-button")
        try await harness.waitUntilAbsent("create-task-title-input")
        try await harness.waitUntilReady()
        // And
        try await harness.openCreateFormFromList()
        // Then
        #expect(harness.mockServer.createTaskRequests.isEmpty)
        #expect(try harness.textFieldValue("create-task-title-input") == "")
    }
    @Test
    func shouldAllowRetryAndCreateTaskAfterInitialPostFailure() async throws {
        // Given
        let context = TaskTestContext(description: nil)
        harness.mockServer
            .enqueueGetTasks()
            .enqueueCreateTaskError(500)
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        // When
        try await harness.clickSubmitCreateForm()
        // Then
        try harness.assertTextEquals("save-error", "Request failed (500)")
        #expect(harness.mockServer.createTaskRequests == [context.createTaskRequest()])
        // When
        try await harness.submitCreateForm()
        // Then
        #expect(harness.mockServer.createTaskRequests == [
            context.createTaskRequest(),
            context.createTaskRequest(),
        ])
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldHaveTranslationsForCreateFlow() async throws {
        // Given
        harness.mockServer.enqueueGetTasksForLanguageSwitch()
        harness.launchApp()
        try await harness.waitUntilReady()
        AppLocale.shared.setLanguage(AppLocale.spanish)
        try await harness.applyCurrentLocale()
        // When
        try await harness.openCreateFormFromList()
        // Then
        try harness.assertTextEquals("modal-title", "Nueva tarea")
        try harness.assertTextEquals("field-title-label", "Título *")
        try harness.assertTextEquals("create-button", "Crear")
    }
    @Test(arguments: GetTasksFailure.allCases)
    func shouldAllowOpeningCreateFormWhenInitialGetTasksFails(failure: GetTasksFailure) async throws {
        // Given
        let context = TaskTestContext()
        failure.enqueue(harness.mockServer)
        harness.mockServer
            .enqueueCreateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        // When
        try await harness.submitCreateForm()
        // Then
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test(arguments: GetTasksFailure.allCases)
    func shouldCloseCreateFormWhenRefreshGetFailsAfterSuccessfulPost(failure: GetTasksFailure) async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks()
            .enqueueCreateTask(context.createTaskResponse())
        failure.enqueue(harness.mockServer)
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        // When
        try await harness.submitCreateForm()
        // Then
        try await harness.assertIsDisplayed("add-task-button")
    }
    @Test(arguments: CreatePostFailure.allCases)
    func shouldDisplayGenericErrorOnCreateFormWhenPostRequestFails(failure: CreatePostFailure) async throws {
        // Given
        let context = TaskTestContext(description: nil)
        harness.mockServer.enqueueGetTasks()
        failure.enqueue(harness.mockServer)
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openCreateFormFromList()
        try await harness.setText("create-task-title-input", value: context.title)
        // When
        try await harness.clickSubmitCreateForm()
        // Then
        try harness.assertTextEquals("save-error", failure.expectedSaveError)
        #expect(harness.mockServer.createTaskRequests == [context.createTaskRequest()])
        try await harness.assertIsDisplayed("create-task-title-input")
    }
}
