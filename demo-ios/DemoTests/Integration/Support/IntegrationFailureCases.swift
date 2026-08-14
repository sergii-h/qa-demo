import Foundation
@testable import Demo

enum CreatePostFailure: CaseIterable {
    case http400
    case http500
    case network

    var expectedSaveError: String {
        switch self {
        case .http400: "Invalid task data"
        case .http500: "Request failed (500)"
        case .network:
            ErrorMessages.mapTaskError(URLError(.notConnectedToInternet), locale: AppLocale.shared)
        }
    }

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http400: server.enqueueCreateTaskError(400)
        case .http500: server.enqueueCreateTaskError(500)
        case .network: server.enqueueCreateTaskNetworkFailure()
        }
    }
}

enum UpdatePutFailure: CaseIterable {
    case http400
    case http500
    case network

    var expectedSaveError: String {
        switch self {
        case .http400: "Invalid task data"
        case .http500: "Request failed (500)"
        case .network:
            ErrorMessages.mapTaskError(URLError(.notConnectedToInternet), locale: AppLocale.shared)
        }
    }

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http400: server.enqueueUpdateTaskError(400)
        case .http500: server.enqueueUpdateTaskError(500)
        case .network: server.enqueueUpdateTaskNetworkFailure()
        }
    }
}

enum DeleteFailure: CaseIterable {
    case http500
    case network

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http500: server.enqueueDeleteTaskError(500)
        case .network: server.enqueueDeleteNetworkFailure()
        }
    }
}

enum GetTasksFailure: CaseIterable {
    case http500
    case network

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http500: server.enqueueGetTasksError(500)
        case .network: server.enqueueGetTasksNetworkFailure()
        }
    }
}

enum GetTaskFailure: CaseIterable {
    case http500
    case network

    var expectedLoadError: String {
        switch self {
        case .http500: "Request failed (500)"
        case .network: Self.emptyTaskDecodingErrorMessage()
        }
    }

    private static func emptyTaskDecodingErrorMessage() -> String {
        do {
            try JSONDecoder().decode(Task.self, from: Data())
            return ""
        } catch {
            return error.localizedDescription
        }
    }

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http500: server.enqueueGetTaskError(500)
        case .network: server.enqueueGetTaskNetworkFailure()
        }
    }
}

enum IsValidFailure: CaseIterable {
    case http500
    case network

    func enqueue(_ server: IntegrationMockServer) {
        switch self {
        case .http500: server.enqueueIsValidError(500)
        case .network: server.enqueueIsValidNetworkFailure()
        }
    }
}
