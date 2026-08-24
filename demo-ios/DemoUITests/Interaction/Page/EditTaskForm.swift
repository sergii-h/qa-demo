import XCTest

final class EditTaskForm: XCUIPage {
    func saveButton() -> XCUIElement {
        element("save-button")
    }

    func titleField() -> XCUIElement {
        element("edit-task-title-input")
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
