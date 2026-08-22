import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
extension StatusChip: Inspectable {}
@MainActor
extension PriorityChip: Inspectable {}
@MainActor
@Suite(.serialized)
struct TaskChipsScreenTests {
    @Test
    func shouldShowTodoStatusLabelWhenTodoStatus() async throws {
        // Given
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(StatusChip(status: .todo))
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        // Then
        let chip = try inspected.find(viewWithAccessibilityIdentifier: "status-tag-TODO")
        #expect(try chip.text().string() == "To Do")
    }
    @Test
    func shouldShowLowPriorityLabelWhenLowPriority() async throws {
        // Given
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(PriorityChip(priority: .low))
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        // Then
        let chip = try inspected.find(viewWithAccessibilityIdentifier: "priority-tag-LOW")
        #expect(try chip.text().string() == "Low")
    }
    @Test
    func shouldShowSpanishStatusLabelWhenLanguageIsSpanish() async throws {
        // Given
        AppLocale.shared.setLanguage(AppLocale.spanish)
        let view = ScreenTestSupport.host(StatusChip(status: .inProgress))
        defer {
            ScreenTestSupport.expel()
            AppLocale.shared.setLanguage(AppLocale.english)
        }
        // When
        let inspected = try view.inspect()
        // Then
        let chip = try inspected.find(viewWithAccessibilityIdentifier: "status-tag-IN_PROGRESS")
        #expect(try chip.text().string() == "En progreso")
    }
    @Test(arguments: TaskStatus.allCases)
    func shouldShowStatusLabelForEachStatus(status: TaskStatus) async throws {
        // Given
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(StatusChip(status: status))
        defer { ScreenTestSupport.expel() }
        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(viewWithAccessibilityIdentifier: "status-tag-\(status.rawValue)")
        }
    }
    @Test(arguments: TaskPriority.allCases)
    func shouldShowPriorityLabelForEachPriority(priority: TaskPriority) async throws {
        // Given
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(PriorityChip(priority: priority))
        defer { ScreenTestSupport.expel() }
        // Then
        #expect(throws: Never.self) {
            _ = try view.inspect().find(viewWithAccessibilityIdentifier: "priority-tag-\(priority.rawValue)")
        }
    }
}
