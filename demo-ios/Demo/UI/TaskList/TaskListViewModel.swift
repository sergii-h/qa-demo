import Foundation
import Observation

struct TaskListUiState: Equatable {
    var tasks: [Task] = []
    var isLoading = true
    var isRefreshing = false
    var errorMessage: String?
    var snackbarMessage: String?
    var deletingTaskIds: Set<String> = []
}

@Observable
@MainActor
final class TaskListViewModel {
    private(set) var uiState = TaskListUiState()

    private let repository: TaskRepositoryProtocol
    private let locale: AppLocale

    init(repository: TaskRepositoryProtocol, locale: AppLocale = .shared, loadsTasksOnInit: Bool = true) {
        self.repository = repository
        self.locale = locale
        if loadsTasksOnInit {
            _Concurrency.Task { await loadTasks() }
        } else {
            uiState.isLoading = false
        }
    }

    func loadTasks() async {
        uiState.isLoading = true
        uiState.errorMessage = nil
        do {
            let tasks = try await repository.getTasks()
            uiState.tasks = tasks
            uiState.isLoading = false
        } catch {
            uiState.tasks = []
            uiState.isLoading = false
            uiState.errorMessage = ErrorMessages.mapTaskError(error, locale: locale)
        }
    }

    func refreshTasks() async {
        guard !uiState.isRefreshing else { return }
        uiState.isRefreshing = true
        uiState.errorMessage = nil
        uiState.snackbarMessage = nil
        do {
            uiState.tasks = try await repository.getTasks()
            uiState.isRefreshing = false
        } catch {
            uiState.isRefreshing = false
            let message = ErrorMessages.mapTaskError(error, locale: locale)
            uiState.errorMessage = message
            uiState.snackbarMessage = message
        }
    }

    func deleteTask(_ task: Task) async {
        uiState.deletingTaskIds.insert(task.id)
        do {
            try await repository.deleteTask(id: task.id)
            uiState.tasks.removeAll { $0.id == task.id }
            uiState.deletingTaskIds.remove(task.id)
        } catch {
            uiState.deletingTaskIds.remove(task.id)
            let message = ErrorMessages.mapTaskError(error, locale: locale)
            uiState.errorMessage = message
            uiState.snackbarMessage = message
        }
    }

    func clearError() {
        uiState.errorMessage = nil
    }

    func clearSnackbar() {
        uiState.snackbarMessage = nil
    }
}
