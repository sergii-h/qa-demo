import XCTest

final class CreateTaskForm: XCUIPage {
    func createButton() -> XCUIElement {
        element("create-button")
    }

    func titleField() -> XCUIElement {
        element("create-task-title-input")
    }

    func descriptionField() -> XCUIElement {
        element("task-description-input")
    }

    func statusDropdown() -> XCUIElement {
        element("status-dropdown")
    }

    func priorityDropdown() -> XCUIElement {
        element("priority-dropdown")
    }

    func statusOption(_ status: TaskStatus) -> XCUIElement {
        element("status-dropdown-option-\(status.rawValue)")
    }

    func priorityOption(_ priority: TaskPriority) -> XCUIElement {
        element("priority-dropdown-option-\(priority.rawValue)")
    }
}
