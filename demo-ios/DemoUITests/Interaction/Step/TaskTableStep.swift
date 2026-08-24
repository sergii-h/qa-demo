import XCTest

final class TaskTableStep {
    private let app: XCUIApplication
    private let mainPage: MainPage
    private let createTaskForm: CreateTaskForm
    private let editTaskForm: EditTaskForm
    private let infoTaskModal: InfoTaskModal

    init(app: XCUIApplication) {
        self.app = app
        mainPage = MainPage(app: app)
        createTaskForm = CreateTaskForm(app: app)
        editTaskForm = EditTaskForm(app: app)
        infoTaskModal = InfoTaskModal(app: app)
    }

    @discardableResult
    func openCreateTaskForm() -> CreateTaskStep {
        Allure.step("Open 'Create task' form") {
            mainPage.createTaskButton().tapWhenReady()
            createTaskForm.titleField().waitUntilExists()
        }
        return CreateTaskStep(app: app)
    }

    func openTaskInfoForm(_ title: String) {
        Allure.step("Open 'Task info' form for task '\(title)'") {
            mainPage.infoButton(taskId: resolveTaskId(title)).tapWhenReady()
            infoTaskModal.waitUntilVisible()
        }
    }

    @discardableResult
    func openTaskEditForm(_ title: String) -> EditTaskStep {
        Allure.step("Open 'Task edit' form for task '\(title)'") {
            mainPage.editButton(taskId: resolveTaskId(title)).tapWhenReady()
            mainPage.waitUntilLoadingSpinnerAbsent()
            editTaskForm.titleField().waitUntilExists()
        }
        return EditTaskStep(app: app)
    }

    func deleteTask(_ title: String) {
        Allure.step("Delete task '\(title)'") {
            let taskId = resolveTaskId(title)
            mainPage.deleteButton(taskId: taskId).tapWhenReady()
            mainPage.waitUntilTaskWithTitleAbsent(title)
        }
    }

    private func resolveTaskId(_ title: String) -> String {
        mainPage.waitUntilTaskWithTitlePresent(title)
        let identifier = mainPage.taskTitleByTitle(title).identifier
        return identifier.replacingOccurrences(of: "task-title-", with: "")
    }
}
