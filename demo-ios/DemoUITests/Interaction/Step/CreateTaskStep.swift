import XCTest

final class CreateTaskStep {
    private let createTaskForm: CreateTaskForm
    private let mainPage: MainPage

    init(app: XCUIApplication) {
        createTaskForm = CreateTaskForm(app: app)
        mainPage = MainPage(app: app)
    }

    @discardableResult
    func setTaskData(_ taskData: TaskData) -> CreateTaskStep {
        Allure.step("Set task data") {
            createTaskForm.typeText("create-task-title-input", taskData.title)
            createTaskForm.typeText("task-description-input", taskData.description)
            if taskData.status != .todo {
                createTaskForm.selectOption(
                    dropdown: createTaskForm.statusDropdown(),
                    option: createTaskForm.statusOption(taskData.status)
                )
            }
            if taskData.priority != .medium {
                createTaskForm.selectOption(
                    dropdown: createTaskForm.priorityDropdown(),
                    option: createTaskForm.priorityOption(taskData.priority)
                )
            }
        }
        return self
    }

    func submitForm() {
        Allure.step("Submit 'Create task' form") {
            createTaskForm.tap("create-button")
            mainPage.waitUntilCreateTaskButtonPresent()
        }
    }
}
