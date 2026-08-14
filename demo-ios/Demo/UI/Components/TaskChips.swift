import SwiftUI

struct StatusChip: View {
    private var locale: AppLocale { AppLocale.shared }
    let status: TaskStatus

    private var color: Color {
        switch status {
        case .todo: TaskColors.todoBlue
        case .inProgress: TaskColors.inProgressOrange
        case .done: TaskColors.doneGreen
        }
    }

    var body: some View {
        chipLabel(TaskLabels.statusLabel(for: status, locale: locale), color: color)
            .testTag("status-tag-\(status.rawValue)")
    }
}

struct PriorityChip: View {
    private var locale: AppLocale { AppLocale.shared }
    let priority: TaskPriority

    private var color: Color {
        switch priority {
        case .low: TaskColors.lowGreen
        case .medium: TaskColors.mediumOrange
        case .high: TaskColors.highRed
        }
    }

    var body: some View {
        chipLabel(TaskLabels.priorityLabel(for: priority, locale: locale), color: color)
            .testTag("priority-tag-\(priority.rawValue)")
    }
}

private func chipLabel(_ text: String, color: Color) -> some View {
    Text(text)
        .font(.caption.weight(.medium))
        .foregroundStyle(color)
        .padding(.horizontal, 10)
        .padding(.vertical, 4)
        .background(color.opacity(0.15))
        .clipShape(Capsule())
}
