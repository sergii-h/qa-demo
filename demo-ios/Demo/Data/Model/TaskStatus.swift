import Foundation

enum TaskStatus: String, Codable, CaseIterable, Sendable {
    case todo = "TODO"
    case inProgress = "IN_PROGRESS"
    case done = "DONE"
}

enum TaskPriority: String, Codable, CaseIterable, Sendable {
    case low = "LOW"
    case medium = "MEDIUM"
    case high = "HIGH"
}
