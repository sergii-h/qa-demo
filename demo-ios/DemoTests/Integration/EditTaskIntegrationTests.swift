import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
final class EditTaskIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldUpdateTaskWithModifiedValuesSendCorrectPutRequestAndShowModifiedTitleInList() async throws {
        // Given
        let context = TaskTestContext(status: .todo, priority: .low)
        let updatedTitle = context.title + " - Updated title"
        let updatedDescription = (context.description ?? "") + " - Updated description"
        let updatedContext = TaskTestContext(
            id: context.id,
            title: updatedTitle,
            description: updatedDescription,
            status: .done,
            priority: .high,
            createdDate: context.createdDate,
            updatedDate: context.updatedDate
        )
        harness.mockServer
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks([updatedContext.createTaskResponse()])
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: updatedTitle)
        try await harness.clearText("task-description-input")
        try await harness.setText("task-description-input", value: updatedDescription)
        try harness.selectStatus(.done)
        try harness.selectPriority(.high)
        // When
        try await harness.submitEditForm()
        // Then
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [updatedContext.createTaskRequest()])
        try harness.assertTextEquals("task-title-\(context.id)", updatedTitle)
    }
    @Test
    func shouldUpdateTaskWithRemovedDescription() async throws {
        // Given
        let context = TaskTestContext()
        let updatedTitle = context.title + " - Updated title"
        let updatedContext = TaskTestContext(
            id: context.id,
            title: updatedTitle,
            description: nil,
            status: context.status,
            priority: context.priority,
            createdDate: context.createdDate,
            updatedDate: context.updatedDate
        )
        harness.mockServer
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks([updatedContext.createTaskResponse()])
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: updatedTitle)
        try await harness.clearText("task-description-input")
        // When
        try await harness.submitEditForm()
        // Then
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [updatedContext.createTaskRequest()])
        try harness.assertTextEquals("task-title-\(context.id)", updatedTitle)
    }
    @Test
    func shouldNotModifyTaskWhenEditFormIsClosedWithoutSaving() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.openEditFormFromList(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: "Unsaved title")
        // When
        try await harness.tap("close-button")
        // And
        try await harness.openEditFormFromList(taskId: context.id)
        // Then
        #expect(harness.mockServer.updateTaskRequests.isEmpty)
        let titleField = try harness.textFieldValue("edit-task-title-input")
        #expect(titleField == context.title)
    }
    @Test
    func shouldProceedWithSaveAfterUserCorrectsInvalidTitle() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(context.createTaskResponse())
            .enqueueGetTasks([context.createTaskResponse()])
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: String(repeating: "a", count: 101))
        // When
        try await harness.clickSubmitEditForm()
        // Then
        try harness.assertTextEquals("title-error", "Title must not exceed 100 characters")
        #expect(harness.mockServer.updateTaskRequests.isEmpty)
        // When
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: context.title)
        try await harness.submitEditForm()
        // Then
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [context.createTaskRequest()])
        try await harness.assertIsDisplayed("task-title-\(context.id)")
    }
    @Test
    func shouldAllowRetryAndSaveTaskAfterInitialPutFailure() async throws {
        // Given
        let context = TaskTestContext()
        let updatedTitle = "Updated title"
        let updatedContext = TaskTestContext(
            id: context.id,
            title: updatedTitle,
            description: context.description,
            status: context.status,
            priority: context.priority,
            createdDate: context.createdDate,
            updatedDate: context.updatedDate
        )
        harness.mockServer
            .enqueueGetTasks([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTaskError(500)
            .enqueueUpdateTask(updatedContext.createTaskResponse())
            .enqueueGetTasks([updatedContext.createTaskResponse()])
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: updatedTitle)
        // When
        try await harness.clickSubmitEditForm()
        // Then
        try harness.assertTextEquals("save-error", "Request failed (500)")
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [updatedContext.createTaskRequest()])
        // When
        try await harness.submitEditForm()
        // Then
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [
            updatedContext.createTaskRequest(),
            updatedContext.createTaskRequest(),
        ])
        try harness.assertTextEquals("task-title-\(context.id)", updatedTitle)
    }
    @Test
    func shouldHaveTranslationsForEditFlow() async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer
            .enqueueGetTasksForLanguageSwitch([context.createTaskResponse()])
            .enqueueGetTask(context.createTaskResponse())
        AppLocale.shared.setLanguage(AppLocale.spanish)
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        // Then
        try harness.assertTextEquals("modal-title", "Editar tarea")
        try harness.assertTextEquals("field-title-label", "Título *")
        try harness.assertTextEquals("save-button", "Guardar")
    }
    @Test(arguments: GetTaskFailure.allCases)
    func shouldAllowOpeningEditFormWhenGetTaskFails(failure: GetTaskFailure) async throws {
        // Given
        let context = TaskTestContext()
        harness.mockServer.enqueueGetTasks([context.createTaskResponse()])
        failure.enqueue(harness.mockServer)
        harness.launchApp()
        try await harness.waitUntilReady()
        try await harness.assertIsDisplayed("task-title-\(context.id)")
        // When
        try await harness.openEditFormFromList(taskId: context.id, expectLoadedTask: false)
        // Then
        #expect(try harness.isButtonDisabled("save-button"))
    }
    @Test(arguments: GetTasksFailure.allCases)
    func shouldCloseEditFormWhenRefreshGetFailsAfterSuccessfulPut(failure: GetTasksFailure) async throws {
        // Given
        let context = TaskTestContext()
        let updatedTitle = context.title + " - Updated title"
        let updatedContext = TaskTestContext(
            id: context.id,
            title: updatedTitle,
            description: context.description,
            status: context.status,
            priority: context.priority,
            createdDate: context.createdDate,
            updatedDate: context.updatedDate
        )
        harness.mockServer
            .enqueueGetTask(context.createTaskResponse())
            .enqueueUpdateTask(updatedContext.createTaskResponse())
        failure.enqueue(harness.mockServer)
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: updatedTitle)
        // When
        try await harness.submitEditForm()
        // Then
        try await harness.assertIsDisplayed("add-task-button")
    }
    @Test(arguments: UpdatePutFailure.allCases)
    func shouldDisplayGenericErrorOnEditFormWhenPutRequestFails(failure: UpdatePutFailure) async throws {
        // Given
        let context = TaskTestContext()
        let updatedTitle = "Updated title"
        let updatedContext = TaskTestContext(
            id: context.id,
            title: updatedTitle,
            description: context.description,
            status: context.status,
            priority: context.priority,
            createdDate: context.createdDate,
            updatedDate: context.updatedDate
        )
        harness.mockServer
            .enqueueGetTask(context.createTaskResponse())
        failure.enqueue(harness.mockServer)
        harness.launchApp(initialPath: [.edit(context.id)])
        try await harness.openEditForm(taskId: context.id)
        try await harness.clearText("edit-task-title-input")
        try await harness.setText("edit-task-title-input", value: updatedTitle)
        // When
        try await harness.clickSubmitEditForm()
        // Then
        try harness.assertTextEquals("save-error", failure.expectedSaveError)
        #expect(harness.mockServer.updateTaskRequests.map { $0.1 } == [updatedContext.createTaskRequest()])
        try await harness.assertIsDisplayed("edit-task-title-input")
    }
}
