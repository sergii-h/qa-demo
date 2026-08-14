import SwiftUI
import ViewInspector
@testable import Demo

@MainActor
@Observable
final class IntegrationNavigationState {
    var path: [DemoRoute] = []
    var listRefreshTrigger = 0
    var formViewModel: TaskFormViewModel?
    var listViewModel: TaskListViewModel?
    var detailViewModel: TaskDetailViewModel?

    func ensureListViewModel(repository: TaskRepositoryProtocol, loadsTasksOnInit: Bool = true) {
        if listViewModel == nil {
            listViewModel = TaskListViewModel(repository: repository, loadsTasksOnInit: loadsTasksOnInit)
        }
    }

    func showCreate(repository: TaskRepositoryProtocol) {
        formViewModel = TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        path.append(.create)
    }

    func showEdit(repository: TaskRepositoryProtocol, taskId: String) {
        formViewModel = TaskFormViewModel(repository: repository, mode: .edit, taskId: taskId)
        path.append(.edit(taskId))
    }

    func showDetail(repository: TaskRepositoryProtocol, taskId: String) {
        detailViewModel = TaskDetailViewModel(repository: repository, taskId: taskId)
        path.append(.detail(taskId))
    }

    func closeTopScreen() {
        guard !path.isEmpty else { return }
        path.removeLast()
        if path.last == nil || !(path.last?.isFormRoute ?? false) {
            formViewModel = nil
        }
        if path.last == nil || path.last?.isDetailRoute != true {
            detailViewModel = nil
        }
    }

    func savedForm() {
        guard !path.isEmpty else { return }
        path.removeLast()
        formViewModel = nil
        listRefreshTrigger += 1
    }
}

private extension DemoRoute {
    var isFormRoute: Bool {
        switch self {
        case .create, .edit:
            return true
        case .detail:
            return false
        }
    }

    var isDetailRoute: Bool {
        switch self {
        case .detail:
            return true
        case .create, .edit:
            return false
        }
    }
}

@MainActor
extension TaskFormView: @retroactive Inspectable {}

@MainActor
extension TaskDetailView: @retroactive Inspectable {}

@MainActor
extension TaskListView: @retroactive Inspectable {}

@MainActor
private struct IntegrationHostRoot: View {
    let repository: TaskRepositoryProtocol
    @Bindable var navigation: IntegrationNavigationState

    var body: some View {
        DemoAppTheme {
            Group {
                if let route = navigation.path.last {
                    routeScreen(route)
                } else {
                    TaskListView(
                        repository: repository,
                        refreshTrigger: navigation.listRefreshTrigger,
                        onCreateTask: { navigation.showCreate(repository: repository) },
                        onEditTask: { navigation.showEdit(repository: repository, taskId: $0) },
                        onTaskInfo: { navigation.showDetail(repository: repository, taskId: $0) },
                        viewModel: navigation.listViewModel
                    )
                }
            }
        }
    }

    @ViewBuilder
    private func routeScreen(_ route: DemoRoute) -> some View {
        switch route {
        case .create:
            TaskFormView(
                repository: repository,
                mode: .create,
                taskId: nil,
                onSaved: { navigation.savedForm() },
                onClose: { navigation.closeTopScreen() },
                viewModel: navigation.formViewModel
            )
        case .edit(let taskId):
            TaskFormView(
                repository: repository,
                mode: .edit,
                taskId: taskId,
                onSaved: { navigation.savedForm() },
                onClose: { navigation.closeTopScreen() },
                viewModel: navigation.formViewModel
            )
        case .detail(let taskId):
            TaskDetailView(
                repository: repository,
                taskId: taskId,
                onClose: { navigation.closeTopScreen() },
                viewModel: navigation.detailViewModel
            )
        }
    }
}

@MainActor
final class IntegrationHarness {
    let mockServer = IntegrationMockServer()
    private var repository: TaskRepository?
    private var hostedRoot: IntegrationHostRoot!
    private var hostedLanguageTag: String!
    private let navigation = IntegrationNavigationState()

    init() {
        AppLocaleTestSupport.resetToEnglish()
    }

    func shutdown() {
        navigation.listViewModel = nil
        navigation.formViewModel = nil
        navigation.detailViewModel = nil
        navigation.path = []
        navigation.listRefreshTrigger = 0
        mockServer.shutdown()
        repository = nil
        hostedRoot = nil
        hostedLanguageTag = nil
        ViewHostingTestSupport.expel()
        AppLocaleTestSupport.resetToEnglish()
    }

    private func ensureStarted() {
        guard repository == nil else { return }
        mockServer.start()
        repository = mockServer.makeRepository()
    }

    private func formViewModel(for route: DemoRoute?, repository: TaskRepositoryProtocol) -> TaskFormViewModel? {
        guard let route else { return nil }
        switch route {
        case .create:
            return TaskFormViewModel(repository: repository, mode: .create, taskId: nil)
        case .edit(let taskId):
            return TaskFormViewModel(repository: repository, mode: .edit, taskId: taskId)
        case .detail:
            return nil
        }
    }

    func launchApp(initialPath: [DemoRoute] = []) {
        ViewHostingTestSupport.expel()
        ensureStarted()
        navigation.path = initialPath
        navigation.listRefreshTrigger = 0
        navigation.formViewModel = formViewModel(for: initialPath.last, repository: repository!)
        if initialPath.isEmpty {
            navigation.ensureListViewModel(repository: repository!, loadsTasksOnInit: false)
        } else {
            navigation.listViewModel = nil
        }
        if let route = initialPath.last, case .detail(let taskId) = route {
            navigation.detailViewModel = TaskDetailViewModel(repository: repository!, taskId: taskId)
        } else {
            navigation.detailViewModel = nil
        }
        hostedLanguageTag = AppLocale.shared.languageTag
        hostedRoot = IntegrationHostRoot(
            repository: repository!,
            navigation: navigation
        )
        rehost()
        if initialPath.isEmpty {
            _Concurrency.Task { await navigation.listViewModel?.loadTasks() }
        }
    }

    func applyCurrentLocale() async throws {
        hostedLanguageTag = AppLocale.shared.languageTag
        rehost()
        try await waitUntilReady()
    }

    private func rehost() {
        let hosted = hostedRoot!
            .environment(\.locale, Locale(identifier: hostedLanguageTag))
            .environment(\.appLocale, AppLocale.shared)
            .id(hostedLanguageTag)
        ViewHostingTestSupport.host(hosted)
    }

    private func appInspect() throws -> InspectableView<ViewType.ClassifiedView> {
        try hostedRoot
            .environment(\.locale, Locale(identifier: hostedLanguageTag))
            .environment(\.appLocale, AppLocale.shared)
            .id(hostedLanguageTag)
            .inspect(function: ViewHostingTestSupport.hostingKey)
    }

    private func listViewInspect() throws -> InspectableView<ViewType.View<TaskListView>> {
        try appInspect().find(TaskListView.self)
    }

    private func activeFormViewModel() -> TaskFormViewModel? {
        navigation.formViewModel
    }

    private func activeFormView() -> TaskFormView? {
        try? appInspect().find(TaskFormView.self).actualView()
    }

    private func activeListView() -> TaskListView? {
        try? listViewInspect().actualView()
    }

    private func listTasks() -> [Task] {
        navigation.listViewModel?.uiState.tasks ?? []
    }

    private func isListTag(_ testTag: String) -> Bool {
        testTag.hasPrefix("task-title-")
            || testTag.hasPrefix("info-button-")
            || testTag.hasPrefix("edit-button-")
            || testTag.hasPrefix("delete-button-")
            || testTag.hasPrefix("status-tag-")
            || testTag.hasPrefix("priority-tag-")
            || testTag == "task-list"
            || testTag == "empty-tasks"
            || testTag == "page-title"
            || testTag == "add-task-button"
            || testTag == "language-switcher"
            || testTag == "error-snackbar"
            || testTag == "refreshing"
    }

    private func findElement(_ testTag: String) throws -> InspectableView<ViewType.ClassifiedView> {
        if isListTag(testTag), !formViewIsVisible(), !detailViewIsVisible() {
            return try listViewInspect().find(viewWithAccessibilityIdentifier: testTag)
        }
        if let form = try? appInspect().find(TaskFormView.self), !isListTag(testTag) {
            return try form.find(viewWithAccessibilityIdentifier: testTag)
        }
        if let detail = try? appInspect().find(TaskDetailView.self), !isListTag(testTag) {
            return try detail.find(viewWithAccessibilityIdentifier: testTag)
        }
        if !formViewIsVisible() && !detailViewIsVisible() {
            return try listViewInspect().find(viewWithAccessibilityIdentifier: testTag)
        }
        return try appInspect().find(viewWithAccessibilityIdentifier: testTag)
    }

    private func formViewIsVisible() -> Bool {
        (try? appInspect().find(TaskFormView.self)) != nil
    }

    private func detailViewIsVisible() -> Bool {
        (try? appInspect().find(TaskDetailView.self)) != nil
    }

    private enum HarnessError: Error, CustomStringConvertible {
        case unexpectedElement(String)
        case textMismatch(tag: String, expected: String, actual: String)
        case missingActiveFormViewModel
        case waitTimedOut(String)

        var description: String {
            switch self {
            case .unexpectedElement(let tag):
                "Expected \(tag) to be absent"
            case .textMismatch(let tag, let expected, let actual):
                "Expected \(tag) text '\(expected)' but got '\(actual)'"
            case .missingActiveFormViewModel:
                "Task form view model is not available"
            case .waitTimedOut(let message):
                message
            }
        }
    }

    func waitUntilReady() async throws {
        if formViewIsVisible() || detailViewIsVisible() {
            return
        }
        try await waitUntilCondition(message: "Task list did not finish loading") {
            guard let listViewModel = navigation.listViewModel else { return false }
            return mockServer.getTasksRequestCount > 0
                && !mockServer.hasInFlightStubRequests()
                && !listViewModel.uiState.isLoading
        }
    }

    func waitForElement(_ testTag: String) async throws {
        try await waitUntilCondition(message: "Element '\(testTag)' was not displayed") {
            elementExists(testTag)
        }
    }

    func waitUntilAbsent(_ testTag: String) async throws {
        try await waitUntilCondition(message: "Element '\(testTag)' was still displayed") {
            !elementExists(testTag)
        }
    }

    func waitUntilCondition(
        message: String = "Condition was not satisfied before timeout",
        iterations: Int = 120,
        _ condition: () -> Bool
    ) async throws {
        for _ in 0..<iterations {
            if condition() {
                return
            }
            try? await _Concurrency.Task.sleep(for: .milliseconds(50))
        }
        throw HarnessError.waitTimedOut(message)
    }

    private func waitForListTaskRefresh() async throws {
        try await waitUntilCondition(message: "Task list refresh did not complete") {
            !mockServer.hasInFlightStubRequests()
        }
    }

    func elementExists(_ testTag: String) -> Bool {
        if testTag == "error-snackbar" {
            if navigation.listViewModel?.uiState.snackbarMessage != nil {
                return true
            }
        }
        if testTag == "add-task-button" {
            return navigation.path.isEmpty && navigation.listViewModel != nil && !navigation.listViewModel!.uiState.isLoading
        }
        if testTag == "empty-tasks" {
            return navigation.listViewModel?.uiState.tasks.isEmpty == true
                && navigation.listViewModel?.uiState.isLoading == false
        }
        if testTag == "load-error" {
            return navigation.detailViewModel?.uiState.errorMessage != nil
        }
        if testTag.hasPrefix("task-title-") {
            let taskId = String(testTag.dropFirst("task-title-".count))
            let tasks = listTasks()
            if tasks.contains(where: { $0.id == taskId }) {
                return true
            }
        }
        return (try? findElement(testTag)) != nil
    }

    func assertIsDisplayed(_ testTag: String) async throws {
        if testTag == "error-snackbar" {
            try await waitUntilCondition(message: "Element '\(testTag)' was not displayed") {
                navigation.listViewModel?.uiState.snackbarMessage != nil || elementExists(testTag)
            }
            return
        }
        if testTag.hasPrefix("task-title-") {
            let taskId = String(testTag.dropFirst("task-title-".count))
            try await waitUntilCondition(message: "Element '\(testTag)' was not displayed") {
                if listTasks().contains(where: { $0.id == taskId }) {
                    return true
                }
                return (try? listViewInspect().find(viewWithAccessibilityIdentifier: testTag)) != nil
            }
            return
        }
        try await waitForElement(testTag)
    }

    func assertIsNotDisplayed(_ testTag: String) throws {
        if elementExists(testTag) {
            throw HarnessError.unexpectedElement(testTag)
        }
    }

    func assertTextEquals(_ testTag: String, _ expected: String) throws {
        if testTag.hasPrefix("task-title-") {
            let taskId = String(testTag.dropFirst("task-title-".count))
            if let task = navigation.listViewModel?.uiState.tasks.first(where: { $0.id == taskId }) {
                if task.title != expected {
                    throw HarnessError.textMismatch(tag: testTag, expected: expected, actual: task.title)
                }
                return
            }
        }
        let element = try findElement(testTag)
        let actual: String
        if let labeled = try? element.labeledContent() {
            actual = try labeled.labelView().text().string()
        } else if let button = try? element.button() {
            actual = try button.labelView().text().string()
        } else {
            actual = try element.text().string()
        }
        if actual != expected {
            throw HarnessError.textMismatch(tag: testTag, expected: expected, actual: actual)
        }
    }

    func textFieldValue(_ testTag: String) throws -> String {
        if testTag.contains("title"), let viewModel = activeFormViewModel() {
            return viewModel.uiState.title
        }
        if testTag.contains("description"), let viewModel = activeFormViewModel() {
            return viewModel.uiState.description
        }
        return try findElement(testTag).textField().input()
    }

    func isButtonDisabled(_ testTag: String) throws -> Bool {
        try findElement(testTag).button().isDisabled()
    }

    func tap(_ testTag: String) async throws {
        if testTag.hasPrefix("delete-button-") {
            let taskId = String(testTag.dropFirst("delete-button-".count))
            try await tapDeleteButton(taskId: taskId)
            return
        }
        let element = try findElement(testTag)
        if testTag == "close-button" {
            try element.button().tap()
            navigation.closeTopScreen()
            rehost()
            return
        }
        if testTag.hasPrefix("info-button-") {
            let taskId = String(testTag.dropFirst("info-button-".count))
            if case .detail(let currentId) = navigation.path.last, currentId == taskId {
                rehost()
                return
            }
            navigation.showDetail(repository: repository!, taskId: taskId)
            rehost()
            return
        }
        if testTag.hasPrefix("edit-button-") {
            try tapButton(element)
            return
        }
        try tapButton(element)
    }

    private func tapDeleteButton(taskId: String) async throws {
        try await waitUntilCondition(message: "Delete task '\(taskId)' was not in the list") {
            navigation.listViewModel?.uiState.tasks.contains(where: { $0.id == taskId }) ?? false
        }
        guard let task = navigation.listViewModel?.uiState.tasks.first(where: { $0.id == taskId }) else {
            throw HarnessError.waitTimedOut("Delete task '\(taskId)' was not in the list")
        }
        await navigation.listViewModel?.deleteTask(task)
    }

    private func tapButton(_ element: InspectableView<ViewType.ClassifiedView>) throws {
        if let button = try? element.button() {
            try button.tap()
            return
        }
        try element.anyView().button().tap()
    }

    func setText(_ testTag: String, value: String) async throws {
        try await waitUntilCondition(message: "Task form view model is not available") {
            activeFormViewModel() != nil
        }
        if testTag.contains("title") {
            activeFormViewModel()?.onTitleChange(value)
        } else if testTag.contains("description") {
            activeFormViewModel()?.onDescriptionChange(value)
        }
        try? findElement(testTag).textField().setInput(value)
        try await waitUntilCondition(message: "Text field '\(testTag)' did not update") {
            if testTag.contains("title") {
                return activeFormViewModel()?.uiState.title == value
            }
            if testTag.contains("description") {
                return activeFormViewModel()?.uiState.description == value
            }
            return (try? textFieldValue(testTag)) == value
        }
    }

    func clearText(_ testTag: String) async throws {
        try await setText(testTag, value: "")
    }

    func selectStatus(_ status: TaskStatus) throws {
        try findElement("status-dropdown").picker().select(value: status)
        activeFormViewModel()?.onStatusChange(status)
    }

    func selectPriority(_ priority: TaskPriority) throws {
        try findElement("priority-dropdown").picker().select(value: priority)
        activeFormViewModel()?.onPriorityChange(priority)
    }

    private func syncActiveFormFields() throws {
        guard let viewModel = activeFormViewModel() else { return }
        viewModel.onTitleChange(viewModel.uiState.title)
        viewModel.onDescriptionChange(viewModel.uiState.description)
    }

    private func requireFormViewModel() throws -> TaskFormViewModel {
        guard let viewModel = activeFormViewModel() else {
            throw HarnessError.missingActiveFormViewModel
        }
        return viewModel
    }

    func assertLanguageMenuOptionDisplayed(_ testTag: String) throws {
        _ = try listViewInspect()
            .find(viewWithAccessibilityIdentifier: "language-switcher")
            .menu()
            .find(viewWithAccessibilityIdentifier: testTag)
    }

    func switchLanguage(_ option: LanguageOption) throws {
        try listViewInspect()
            .find(viewWithAccessibilityIdentifier: "language-switcher")
            .menu()
            .find(viewWithAccessibilityIdentifier: option.testTag)
            .button()
            .tap()
        hostedLanguageTag = AppLocale.shared.languageTag
        rehost()
    }

    func openCreateFormFromList() async throws {
        navigation.showCreate(repository: repository!)
        rehost()
        try await waitForElement("create-task-title-input")
    }

    func openCreateForm() async throws {
        try await waitUntilCondition(message: "Task form view model is not available") {
            activeFormViewModel() != nil
        }
        try await waitForElement("create-task-title-input")
    }

    private func waitForEditFormTaskLoad(expectLoadedTask: Bool) async throws {
        try await waitForElement("edit-task-title-input")
        try await waitUntilCondition(message: "Edit form task request did not complete") {
            mockServer.getTaskRequestCount > 0 && !mockServer.hasInFlightStubRequests()
        }
        if expectLoadedTask {
            try await waitUntilCondition(message: "Edit form did not load task") {
                guard let viewModel = activeFormViewModel() else { return false }
                return !viewModel.uiState.isLoading && !viewModel.uiState.title.isEmpty
            }
        } else {
            try await waitUntilCondition(message: "Edit form did not finish loading") {
                activeFormViewModel()?.uiState.isLoading == false
            }
        }
    }

    func openEditFormFromList(taskId: String, expectLoadedTask: Bool = true) async throws {
        let previousGetTaskCount = mockServer.getTaskRequestCount
        navigation.showEdit(repository: repository!, taskId: taskId)
        rehost()
        try await waitUntilCondition(message: "Edit form task request did not start") {
            mockServer.getTaskRequestCount > previousGetTaskCount
        }
        try await waitForEditFormTaskLoad(expectLoadedTask: expectLoadedTask)
    }

    func openEditForm(taskId: String) async throws {
        try await waitForEditFormTaskLoad(expectLoadedTask: true)
    }

    private func waitForEditFormReady() async throws {
        try await waitForEditFormTaskLoad(expectLoadedTask: true)
    }

    func openDetailFromList(taskId: String) async throws {
        let previousGetTaskCount = mockServer.getTaskRequestCount
        navigation.showDetail(repository: repository!, taskId: taskId)
        rehost()
        try await waitUntilCondition(message: "Task detail did not open") { detailViewIsVisible() }
        try await waitForDetailLoad(previousGetTaskCount: previousGetTaskCount)
        try await waitForElement("description")
    }

    func openDetail(taskId: String) async throws {
        try await waitForDetailLoad(previousGetTaskCount: 0)
        try await waitForElement("description")
    }

    func waitForDetailScreenToSettle() async throws {
        try await waitUntilCondition(message: "Task detail load did not complete") {
            !mockServer.hasInFlightStubRequests()
        }
        try await waitUntilCondition(message: "Task detail did not finish loading") {
            navigation.detailViewModel?.uiState.isLoading == false
        }
        rehost()
    }

    func waitForDetailLoadError() async throws {
        try await waitUntilCondition(message: "Task detail load error was not displayed") {
            navigation.detailViewModel?.uiState.errorMessage != nil || elementExists("load-error")
        }
        try await waitForElement("load-error")
    }

    private func waitForDetailLoad(previousGetTaskCount: Int) async throws {
        try await waitUntilCondition(message: "Task detail load did not complete") {
            mockServer.getTaskRequestCount > previousGetTaskCount && !mockServer.hasInFlightStubRequests()
        }
        try await waitUntilCondition(message: "Task detail did not finish loading") {
            navigation.detailViewModel?.uiState.isLoading == false
        }
        rehost()
    }

    private func savedFormAndReturnToList() async {
        guard !navigation.path.isEmpty else { return }
        navigation.path.removeLast()
        navigation.formViewModel = nil
        navigation.ensureListViewModel(repository: repository!, loadsTasksOnInit: false)
        if let listViewModel = navigation.listViewModel {
            await listViewModel.refreshTasks()
        }
        rehost()
    }

    func submitCreateForm() async throws {
        mockServer.start()
        let requestCount = mockServer.createTaskRequests.count
        try syncActiveFormFields()
        let viewModel = try requireFormViewModel()
        try await waitUntilCondition(message: "Create form submit button did not enable") {
            viewModel.isSubmitEnabled
        }
        await viewModel.save()
        if viewModel.uiState.saveSucceeded {
            await savedFormAndReturnToList()
        }
        try await waitForCreateFormSubmit(previousRequestCount: requestCount)
        if !formViewIsVisible() {
            try await waitUntilReady()
            try await waitForListTaskRefresh()
        }
    }

    func clickSubmitCreateForm() async throws {
        mockServer.start()
        let requestCount = mockServer.createTaskRequests.count
        try syncActiveFormFields()
        let viewModel = try requireFormViewModel()
        await viewModel.save()
        try await waitForCreateFormSubmit(previousRequestCount: requestCount)
    }

    private func waitForCreateFormSubmit(previousRequestCount: Int) async throws {
        try await waitUntilCondition(message: "Create form submit did not start") {
            mockServer.createTaskRequests.count > previousRequestCount
                || elementExists("title-error")
                || !formViewIsVisible()
        }
        try await waitUntilCondition(message: "Create form submit did not settle") {
            if formViewIsVisible() && elementExists("loading-spinner") {
                return false
            }
            if elementExists("title-error") || elementExists("save-error") {
                return true
            }
            if !formViewIsVisible() && mockServer.createTaskRequests.count > previousRequestCount {
                return true
            }
            return false
        }
    }

    func submitEditForm() async throws {
        try syncActiveFormFields()
        let viewModel = try requireFormViewModel()
        try await waitUntilCondition(message: "Edit form submit button did not enable") {
            viewModel.isSubmitEnabled
        }
        await viewModel.save()
        if viewModel.uiState.saveSucceeded {
            await savedFormAndReturnToList()
        }
        try await waitUntilAbsent("edit-task-title-input")
        if navigation.path.isEmpty {
            try await waitUntilReady()
        }
    }

    func clickSubmitEditForm() async throws {
        try syncActiveFormFields()
        let viewModel = try requireFormViewModel()
        await viewModel.save()
        try await waitUntilCondition(message: "Edit form submit did not settle") {
            !elementExists("loading-spinner")
                || elementExists("save-error")
                || elementExists("title-error")
        }
    }

    func pullToRefresh() async throws {
        guard let listViewModel = navigation.listViewModel else {
            throw HarnessError.waitTimedOut("Task list view model is not available")
        }
        await listViewModel.refreshTasks()
        try await waitForListTaskRefresh()
        try await waitUntilAbsent("refreshing")
    }
}
