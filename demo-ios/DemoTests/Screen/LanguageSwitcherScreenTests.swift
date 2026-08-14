import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
extension LanguageSwitcher: Inspectable {}
@MainActor
@Suite(.serialized)
struct LanguageSwitcherScreenTests {
    @Test
    func shouldShowEnLabelWhenEnglishSelected() async throws {
        // Given
        ScreenTestSupport.resetLocaleToEnglish()
        let view = ScreenTestSupport.host(LanguageSwitcher())
        defer { ScreenTestSupport.expel() }
        // When
        let inspected = try view.inspect()
        // Then
        #expect(throws: Never.self) {
            _ = try inspected.find(viewWithAccessibilityIdentifier: "language-switcher")
        }
    }
    @Test
    func shouldShowEsLabelWhenSpanishSelected() async throws {
        // Given
        AppLocale.shared.setLanguage(AppLocale.spanish)
        let view = ScreenTestSupport.host(LanguageSwitcher())
        defer {
            ScreenTestSupport.expel()
            AppLocale.shared.setLanguage(AppLocale.english)
        }
        // When
        let label = try view.inspect().find(viewWithAccessibilityIdentifier: "language-switcher")
        // Then
        #expect(try label.menu().labelView().text().string() == "ES")
    }
}
