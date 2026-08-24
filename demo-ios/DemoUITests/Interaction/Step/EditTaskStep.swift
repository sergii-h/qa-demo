import XCTest

final class EditTaskStep {
    private let app: XCUIApplication
    private let editTaskForm: EditTaskForm
    private let mainPage: MainPage

    init(app: XCUIApplication) {
        self.app = app
        editTaskForm = EditTaskForm(app: app)
        mainPage = MainPage(app: app)
    }

    @discardableResult
    func setTaskData(_ taskData: TaskData) -> EditTaskStep {
        Allure.step("Set task data") {
            editTaskForm.replaceText("edit-task-title-input", with: taskData.title)
            editTaskForm.replaceText("task-description-input", with: taskData.description)
            editTaskForm.selectOption(
                dropdown: editTaskForm.statusDropdown(),
                option: editTaskForm.statusOption(taskData.status)
            )
            editTaskForm.selectOption(
                dropdown: editTaskForm.priorityDropdown(),
                option: editTaskForm.priorityOption(taskData.priority)
            )
        }
        return self
    }

    @discardableResult
    func submitForm() -> TaskTableStep {
        Allure.step("Submit 'Edit task' form") {
            editTaskForm.tap("save-button")
            mainPage.waitUntilCreateTaskButtonPresent()
        }
        return TaskTableStep(app: app)
    }
}
