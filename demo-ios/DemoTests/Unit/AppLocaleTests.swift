import Testing
@testable import Demo

@Suite(.serialized)
struct AppLocaleTests {
    @Test
    func shouldReturnSpanishStringWhenLanguageIsSpanish() {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.english)

        // When
        locale.setLanguage(AppLocale.spanish)

        // Then
        #expect(locale.localizedString("tasks_title") == "Tareas")
        locale.setLanguage(AppLocale.english)
    }

    @Test
    func shouldReturnEnglishStringWhenLanguageIsEnglish() {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.spanish)

        // When
        locale.setLanguage(AppLocale.english)

        // Then
        #expect(locale.localizedString("tasks_title") == "Tasks")
    }
}
