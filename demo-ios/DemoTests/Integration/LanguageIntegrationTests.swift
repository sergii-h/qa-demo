import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
@Suite(.serialized)
final class LanguageIntegrationTests {
    let harness = IntegrationHarness()

    deinit {
        MainActor.assumeIsolated {
            harness.shutdown()
        }
    }

    @Test
    func shouldRenderLanguageSwitcherWithEnAndEsOptions() async throws {
        // Given
        harness.mockServer.enqueueGetTasks()
        harness.launchApp()
        try await harness.waitUntilReady()
        // Then
        try await harness.assertIsDisplayed("language-switcher")
        try harness.assertLanguageMenuOptionDisplayed("language-option-en")
        try harness.assertLanguageMenuOptionDisplayed("language-option-es")
    }
    @Test
    func shouldChangeCurrentLanguageWhenUserSelectsAnotherLanguageOption() async throws {
        // Given
        harness.mockServer.enqueueGetTasksForLanguageSwitch()
        harness.launchApp()
        try await harness.waitUntilReady()
        // When
        try harness.switchLanguage(.es)
        try await harness.waitUntilReady()
        // Then
        try harness.assertTextEquals("page-title", "Tareas")
        // When
        harness.mockServer.enqueueGetTasks()
        try harness.switchLanguage(.en)
        try await harness.waitUntilReady()
        // Then
        try harness.assertTextEquals("page-title", "Tasks")
    }
}
