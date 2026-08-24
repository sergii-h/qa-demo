enum TaskStatus: String, Codable, CaseIterable {
    case todo = "TODO"
    case inProgress = "IN_PROGRESS"
    case done = "DONE"
}

enum TaskPriority: String, Codable, CaseIterable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
}
