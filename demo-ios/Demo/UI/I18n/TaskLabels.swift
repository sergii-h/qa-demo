import Foundation

enum TaskLabels {
    static func statusLabel(for status: TaskStatus, locale: AppLocale) -> String {
        switch status {
        case .todo:
            return locale.localizedString("status_todo")
        case .inProgress:
            return locale.localizedString("status_in_progress")
        case .done:
            return locale.localizedString("status_done")
        }
    }

    static func priorityLabel(for priority: TaskPriority, locale: AppLocale) -> String {
        switch priority {
        case .low:
            return locale.localizedString("priority_low")
        case .medium:
            return locale.localizedString("priority_medium")
        case .high:
            return locale.localizedString("priority_high")
        }
    }
}
