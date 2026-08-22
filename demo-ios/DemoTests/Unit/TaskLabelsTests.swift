import Testing
@testable import Demo

@Suite(.serialized)
@MainActor
struct TaskLabelsTests {
    @Test
    func shouldReturnTodoLabelWhenTodoStatusAndEnglish() {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.english)

        // When
        let label = TaskLabels.statusLabel(for: .todo, locale: locale)

        // Then
        #expect(label == "To Do")
    }

    @Test
    func shouldReturnHighPriorityLabelWhenHighPriorityAndEnglish() {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.english)

        // When
        let label = TaskLabels.priorityLabel(for: .high, locale: locale)

        // Then
        #expect(label == "High")
    }

    @Test
    func shouldReturnSpanishDoneLabelWhenDoneStatusAndSpanish() {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.spanish)

        // When
        let label = TaskLabels.statusLabel(for: .done, locale: locale)

        // Then
        #expect(label == "Hecho")
        locale.setLanguage(AppLocale.english)
    }

    @Test(arguments: [
        (TaskStatus.todo, "To Do"),
        (TaskStatus.inProgress, "In Progress"),
        (TaskStatus.done, "Done"),
    ])
    func shouldReturnEnglishStatusLabel(status: TaskStatus, expected: String) {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.english)

        // Then
        #expect(TaskLabels.statusLabel(for: status, locale: locale) == expected)
    }

    @Test(arguments: [
        (TaskPriority.low, "Low"),
        (TaskPriority.medium, "Medium"),
        (TaskPriority.high, "High"),
    ])
    func shouldReturnEnglishPriorityLabel(priority: TaskPriority, expected: String) {
        // Given
        let locale = AppLocale.shared
        locale.setLanguage(AppLocale.english)

        // Then
        #expect(TaskLabels.priorityLabel(for: priority, locale: locale) == expected)
    }
}
