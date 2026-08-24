import XCTest

final class InfoTaskModal: XCUIPage {
    func title() -> XCUIElement {
        element("modal-title")
    }

    func validIcon() -> XCUIElement {
        element("valid")
    }

    func descriptionField() -> XCUIElement {
        element("description")
    }

    func statusTag(_ status: TaskStatus) -> XCUIElement {
        element("status-tag-\(status.rawValue)")
    }

    func priorityTag(_ priority: TaskPriority) -> XCUIElement {
        element("priority-tag-\(priority.rawValue)")
    }

    func waitUntilVisible(timeout: TimeInterval = 15) {
        waitUntilPresent("modal-title", timeout: timeout)
        waitUntilAbsent("loading-spinner", timeout: timeout)
        waitUntilPresent("description", timeout: timeout)
    }
}
