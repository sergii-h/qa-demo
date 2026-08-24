import SwiftUI

struct TaskDetailView: View {
    private var locale: AppLocale { AppLocale.shared }
    @Environment(\.dismiss) private var dismiss
    @State var viewModel: TaskDetailViewModel
    let onClose: (() -> Void)?

    init(
        repository: TaskRepositoryProtocol,
        taskId: String,
        onClose: (() -> Void)? = nil,
        viewModel: TaskDetailViewModel? = nil
    ) {
        _viewModel = State(
            initialValue: viewModel ?? TaskDetailViewModel(repository: repository, taskId: taskId)
        )
        self.onClose = onClose
    }

    var body: some View {
        Form {
            if viewModel.uiState.isLoading {
                ProgressView()
                    .frame(maxWidth: .infinity)
                    .testTag("loading-spinner")
            } else if let errorMessage = viewModel.uiState.errorMessage {
                Text(errorMessage)
                    .foregroundStyle(.red)
                    .testTag("load-error")
            } else if let task = viewModel.uiState.task {
                Section {
                    Text(locale.localizedString("detail_description"))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .testTag("detail-description-label")
                    Text(task.description ?? locale.localizedString("no_description"))
                        .testTag("description")

                    if let createdDate = task.createdDate {
                        LabeledContent(locale.localizedString("detail_created")) {
                            Text(TaskDateFormatter.format(createdDate, locale: locale))
                                .testTag("created-date")
                        }
                    }

                    if let updatedDate = task.updatedDate {
                        LabeledContent(locale.localizedString("detail_last_updated")) {
                            Text(TaskDateFormatter.format(updatedDate, locale: locale))
                                .testTag("updated-date")
                        }
                    }

                    Text(locale.localizedString("detail_validated"))
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .testTag("detail-validated-label")
                    HStack {
                        Image(systemName: viewModel.uiState.isValid ? "checkmark.circle.fill" : "xmark.circle.fill")
                            .foregroundStyle(viewModel.uiState.isValid ? .green : .red)
                        Text(viewModel.uiState.isValid
                            ? locale.localizedString("valid")
                            : locale.localizedString("not_valid"))
                    }
                    .testTag(viewModel.uiState.isValid ? "valid" : "notValid")
                    .accessibilityElement(children: .combine)
                    .accessibilityLabel(
                        viewModel.uiState.isValid
                            ? locale.localizedString("valid")
                            : locale.localizedString("not_valid")
                    )

                    HStack {
                        StatusChip(status: task.status)
                        PriorityChip(priority: task.priority)
                    }
                }
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

    private var modalTitle: String {
        viewModel.uiState.task?.title ?? locale.localizedString("task_info")
    }
}
