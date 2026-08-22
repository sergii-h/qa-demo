import Foundation
import Observation

enum TaskFormMode: Equatable {
    case create
    case edit
}

struct TaskFormUiState: Equatable {
    var mode: TaskFormMode = .create
    var isLoading = false
    var isSaving = false
    var title = ""
    var description = ""
    var status: TaskStatus = .todo
    var priority: TaskPriority = .medium
    var titleError: String?
    var saveError: String?
    var saveSucceeded = false

    var isSubmitEnabled: Bool {
        !title.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty && !isSaving
    }
}

@Observable
@MainActor
final class TaskFormViewModel {
    private(set) var uiState = TaskFormUiState()

    private let repository: TaskRepositoryProtocol
    private let locale: AppLocale
    private let taskId: String?
    private let mode: TaskFormMode

    init(
        repository: TaskRepositoryProtocol,
        mode: TaskFormMode,
        taskId: String?,
        locale: AppLocale = .shared,
        loadsTaskOnInit: Bool = true
    ) {
        self.repository = repository
        self.mode = mode
        self.taskId = taskId
        self.locale = locale
        uiState.mode = mode
        if loadsTaskOnInit, mode == .edit, let taskId {
            _Concurrency.Task { await loadTask(taskId) }
        }
    }

    func loadTask(_ taskId: String) async {
        uiState.isLoading = true
        do {
            let task = try await repository.getTask(id: taskId)
            uiState.isLoading = false
            uiState.title = task.title
            uiState.description = task.description ?? ""
            uiState.status = task.status
            uiState.priority = task.priority
        } catch {
            uiState.isLoading = false
            uiState.title = ""
            uiState.description = ""
            uiState.status = .todo
            uiState.priority = .medium
        }
    }

    func onTitleChange(_ value: String) {
        uiState.title = value
        uiState.titleError = nil
    }

    func onDescriptionChange(_ value: String) {
        uiState.description = value
    }

    func onStatusChange(_ value: TaskStatus) {
        uiState.status = value
    }

    func onPriorityChange(_ value: TaskPriority) {
        uiState.priority = value
    }

    var isSubmitEnabled: Bool {
        uiState.isSubmitEnabled
    }

    func save() async {
        let trimmedTitle = uiState.title.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !trimmedTitle.isEmpty else { return }
        if trimmedTitle.count > TaskValidation.maxTitleLength {
            uiState.titleError = locale.localizedString("error_title_too_long")
            return
        }

        let request = TaskRequest(
            title: trimmedTitle,
            description: uiState.description.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
                ? nil
                : uiState.description.trimmingCharacters(in: .whitespacesAndNewlines),
            status: uiState.status,
            priority: uiState.priority
        )

        uiState.isSaving = true
        uiState.titleError = nil
        uiState.saveError = nil

        do {
            switch mode {
            case .create:
                _ = try await repository.createTask(request)
            case .edit:
                guard let taskId else { return }
                _ = try await repository.updateTask(id: taskId, request: request)
            }
            uiState.isSaving = false
            uiState.saveSucceeded = true
        } catch {
            uiState.isSaving = false
            if ErrorMessages.isDuplicateTitleError(error) {
                uiState.titleError = locale.localizedString("error_title_already_exists")
            } else {
                uiState.saveError = ErrorMessages.mapTaskError(error, locale: locale)
            }
        }
    }
}
