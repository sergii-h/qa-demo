import SwiftUI

struct TaskFormView: View {
    private var locale: AppLocale { AppLocale.shared }
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: TaskFormViewModel

    let onSaved: () -> Void
    let onClose: (() -> Void)?

    init(
        repository: TaskRepositoryProtocol,
        mode: TaskFormMode,
        taskId: String?,
        onSaved: @escaping () -> Void,
        onClose: (() -> Void)? = nil,
        viewModel: TaskFormViewModel? = nil
    ) {
        _viewModel = State(
            initialValue: viewModel ?? TaskFormViewModel(
                repository: repository,
                mode: mode,
                taskId: taskId
            )
        )
        self.onSaved = onSaved
        self.onClose = onClose
    }

    private var titleInputTag: String {
        viewModel.uiState.mode == .create ? "create-task-title-input" : "edit-task-title-input"
    }

    private var modalTitle: String {
        switch viewModel.uiState.mode {
        case .create:
            return locale.localizedString("new_task")
        case .edit:
            return locale.localizedString("edit_task")
        }
    }

    var body: some View {
        Form {
            if viewModel.uiState.isLoading || viewModel.uiState.isSaving {
                Section {
                    ProgressView()
                        .frame(maxWidth: .infinity)
                        .testTag("loading-spinner")
                }
            }

            Section {
                Text(locale.localizedString("field_title"))
                    .testTag("field-title-label")
                TextField(locale.localizedString("field_title"), text: Binding(
                    get: { viewModel.uiState.title },
                    set: { viewModel.onTitleChange($0) }
                ))
                .testTag(titleInputTag)

                if let titleError = viewModel.uiState.titleError {
                    Text(titleError)
                        .foregroundStyle(.red)
                        .font(.caption)
                        .testTag("title-error")
                }

                Text(locale.localizedString("field_description"))
                TextField(locale.localizedString("field_description"), text: Binding(
                    get: { viewModel.uiState.description },
                    set: { viewModel.onDescriptionChange($0) }
                ), axis: .vertical)
                .testTag("task-description-input")
            }

            Section {
                Picker(locale.localizedString("field_status"), selection: Binding(
                    get: { viewModel.uiState.status },
                    set: { viewModel.onStatusChange($0) }
                )) {
                    ForEach(TaskStatus.allCases, id: \.self) { status in
                        Text(TaskLabels.statusLabel(for: status, locale: locale))
                            .tag(status)
                    }
                }
                .testTag("status-dropdown")

                Picker(locale.localizedString("field_priority"), selection: Binding(
                    get: { viewModel.uiState.priority },
                    set: { viewModel.onPriorityChange($0) }
                )) {
                    ForEach(TaskPriority.allCases, id: \.self) { priority in
                        Text(TaskLabels.priorityLabel(for: priority, locale: locale))
                            .tag(priority)
                    }
                }
                .testTag("priority-dropdown")
            }

            if let saveError = viewModel.uiState.saveError {
                Text(saveError)
                    .foregroundStyle(.red)
                    .testTag("save-error")
            }

            Section {
                Button {
                    _Concurrency.Task {
                        await viewModel.save()
                        if viewModel.uiState.saveSucceeded {
                            onSaved()
                        }
                    }
                } label: {
                    Text(viewModel.uiState.mode == .create
                        ? locale.localizedString("create")
                        : locale.localizedString("save"))
                        .frame(maxWidth: .infinity, alignment: .center)
                }
                .buttonStyle(.borderedProminent)
                .tint(TaskColors.primary)
                .disabled(!viewModel.isSubmitEnabled)
                .testTag(viewModel.uiState.mode == .create ? "create-button" : "save-button")
                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
            }
        }
        .navigationBarTitleDisplayMode(.inline)
        .navigationBarBackButtonHidden(true)
        .toolbar {
            ToolbarItem(placement: .topBarLeading) {
                Button {
                    if let onClose {
                        onClose()
                    } else {
                        dismiss()
                    }
                } label: {
                    Label(locale.localizedString("back"), systemImage: "chevron.left")
                        .labelStyle(.iconOnly)
                        .font(.body.weight(.semibold))
                }
                .testTag("close-button")
                .accessibilityLabel(locale.localizedString("back"))
            }
            ToolbarItem(placement: .principal) {
                Text(modalTitle)
                    .font(.headline)
                    .testTag("modal-title")
            }
        }
    }
}
