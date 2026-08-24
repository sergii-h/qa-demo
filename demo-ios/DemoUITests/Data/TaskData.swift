struct TaskData: Equatable {
    let title: String
    let description: String
    let status: TaskStatus
    let priority: TaskPriority

    var allureLabel: String {
        "{title: \(title), description: \(description), status: \(status.rawValue), priority: \(priority.rawValue)}"
    }
}
