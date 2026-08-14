import Foundation
import Observation

struct TaskDetailUiState: Equatable {
    var isLoading = true
    var task: Task?
    var isValid = false
    var errorMessage: String?
}

@Observable
@MainActor
final class TaskDetailViewModel {
    private(set) var uiState = TaskDetailUiState()

    private let repository: TaskRepositoryProtocol
    private let locale: AppLocale
    private let taskId: String

    init(
        repository: TaskRepositoryProtocol,
        taskId: String,
        locale: AppLocale = .shared,
        loadsOnInit: Bool = true
    ) {
        self.repository = repository
        self.taskId = taskId
        self.locale = locale
        if loadsOnInit {
            _Concurrency.Task { await load() }
        } else {
            uiState.isLoading = false
        }
    }

    func load() async {
        uiState.isLoading = true
        uiState.errorMessage = nil

        var loadError: Error?
        var loadedTask: Task?
        do {
            loadedTask = try await repository.getTask(id: taskId)
        } catch {
            loadError = error
        }

        let isValid = (try? await repository.isValid(id: taskId)) ?? false

        uiState.isLoading = false
        uiState.task = loadedTask
        uiState.isValid = isValid
        if let loadError {
            uiState.errorMessage = ErrorMessages.mapTaskError(loadError, locale: locale)
        }
    }
}
