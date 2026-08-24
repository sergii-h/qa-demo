import SwiftUI

struct TaskListView: View {
    private var locale: AppLocale { AppLocale.shared }
    @State var viewModel: TaskListViewModel

    let onCreateTask: () -> Void
    let onEditTask: (String) -> Void
    let onTaskInfo: (String) -> Void
    let refreshTrigger: Int

    init(
        repository: TaskRepositoryProtocol,
        refreshTrigger: Int,
        onCreateTask: @escaping () -> Void,
        onEditTask: @escaping (String) -> Void,
        onTaskInfo: @escaping (String) -> Void,
        viewModel: TaskListViewModel? = nil
    ) {
        _viewModel = State(initialValue: viewModel ?? TaskListViewModel(repository: repository))
        self.refreshTrigger = refreshTrigger
        self.onCreateTask = onCreateTask
        self.onEditTask = onEditTask
        self.onTaskInfo = onTaskInfo
    }

    var body: some View {
        ZStack(alignment: .bottomTrailing) {
            taskListContent

            Button(action: onCreateTask) {
                Text("+")
                    .font(.title2.weight(.semibold))
                    .foregroundStyle(.white)
                    .frame(width: 56, height: 56)
                    .background(TaskColors.primary)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
            }
            .testTag("add-task-button")
            .accessibilityLabel(locale.localizedString("create_task"))
            .padding(16)
        }
        .background(TaskColors.background)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .principal) {
                Text(locale.localizedString("tasks_title"))
                    .font(.headline)
                    .testTag("page-title")
            }
            ToolbarItem(placement: .topBarTrailing) {
                LanguageSwitcher()
            }
        }
        .overlay(alignment: .top) {
            if viewModel.uiState.isRefreshing {
                ProgressView()
                    .padding(8)
                    .background(.thinMaterial)
                    .clipShape(Capsule())
                    .padding(.top, 8)
                    .testTag("refreshing")
            }
        }
        .overlay(alignment: .bottom) {
            if let snackbarMessage = viewModel.uiState.snackbarMessage {
                Text(snackbarMessage)
                    .font(.subheadline)
                    .foregroundStyle(.white)
                    .padding()
                    .background(.thinMaterial)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .padding()
                    .testTag("error-snackbar")
            }
        }
        .onChange(of: refreshTrigger) { _, newValue in
            guard newValue > 0 else { return }
            _Concurrency.Task { await viewModel.refreshTasks() }
        }
        .task(id: viewModel.uiState.snackbarMessage) {
            guard viewModel.uiState.snackbarMessage != nil else { return }
            try? await _Concurrency.Task.sleep(for: .seconds(3))
            viewModel.clearSnackbar()
        }
    }

    @ViewBuilder
    private var taskListContent: some View {
        ScrollView {
            Group {
                if viewModel.uiState.isLoading {
                    ProgressView()
                        .frame(maxWidth: .infinity, minHeight: 200)
                        .testTag("loading-spinner")
                } else if viewModel.uiState.tasks.isEmpty {
                    Text(locale.localizedString("empty_tasks"))
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity, minHeight: 200)
                        .testTag("empty-tasks")
                } else {
                    LazyVStack(spacing: 12) {
                        ForEach(viewModel.uiState.tasks, id: \.id) { task in
                            taskCard(task)
                        }
                    }
                    .padding(16)
                }
            }
        }
        .testTag("task-list")
        .refreshable {
            await viewModel.refreshTasks()
        }
    }

    private func taskCard(_ task: Task) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(task.title)
                .font(.title3.weight(.semibold))
                .testTag("task-title-\(task.id)")

            HStack(spacing: 8) {
                StatusChip(status: task.status)
                PriorityChip(priority: task.priority)
            }

            HStack {
                Spacer()
                taskIconButton(
                    systemImage: "info.circle",
                    label: locale.localizedString("action_info"),
                    testTag: "info-button-\(task.id)",
                    action: { onTaskInfo(task.id) }
                )
                taskIconButton(
                    systemImage: "pencil",
                    label: locale.localizedString("action_edit"),
                    testTag: "edit-button-\(task.id)",
                    action: { onEditTask(task.id) }
                )
                taskIconButton(
                    systemImage: "trash",
                    label: locale.localizedString("action_delete"),
                    testTag: "delete-button-\(task.id)",
                    action: {
                        _Concurrency.Task { await viewModel.deleteTask(task) }
                    },
                    isDestructive: true,
                    isDisabled: viewModel.uiState.deletingTaskIds.contains(task.id)
                )
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(TaskColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 12))
        .shadow(color: .black.opacity(0.08), radius: 4, x: 0, y: 2)
    }

    private func taskIconButton(
        systemImage: String,
        label: String,
        testTag: String,
        action: @escaping () -> Void,
        isDestructive: Bool = false,
        isDisabled: Bool = false
    ) -> some View {
        Button(action: action) {
            Label(label, systemImage: systemImage)
                .labelStyle(.iconOnly)
                .font(.title3)
                .foregroundStyle(isDestructive ? TaskColors.highRed : TaskColors.primary)
                .frame(width: 44, height: 44)
        }
        .buttonStyle(.plain)
        .disabled(isDisabled)
        .testTag(testTag)
        .accessibilityLabel(label)
    }
}
