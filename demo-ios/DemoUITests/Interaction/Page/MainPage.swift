import XCTest

struct TaskTestContext {
    let id = UUID().uuidString
    let title = "Task \(UUID().uuidString.prefix(8))"
    let description = "E2E description"

    func stubTask() -> [String: Any] {
        [
            "id": id,
            "title": title,
            "description": description,
            "status": "TODO",
            "priority": "MEDIUM",
            "createdDate": "2024-01-15T10:00:00.000Z",
            "updatedDate": "2024-01-16T12:00:00.000Z",
        ]
    }
}

final class MainPage {
    private let app: XCUIApplication

    init(app: XCUIApplication) {
        self.app = app
    }

    func waitUntilReady() {
        let list = app.otherElements["task-list"]
        let empty = app.staticTexts["empty-tasks"]
        _ = list.waitForExistence(timeout: 10) || empty.waitForExistence(timeout: 10)
    }

    func tapAddTask() {
        app.buttons["add-task-button"].tap()
    }

    func hasTask(id: String, titled title: String) -> Bool {
        let element = app.descendants(matching: .any)["task-title-\(id)"]
        guard element.waitForExistence(timeout: 10) else {
            return false
        }
        return element.label == title
    }
}

final class CreateTaskForm {
    private let app: XCUIApplication

    init(app: XCUIApplication) {
        self.app = app
    }

    func waitUntilReady() {
        _ = app.textFields["create-task-title-input"].waitForExistence(timeout: 5)
    }

    func setTitle(_ title: String) {
        let field = app.textFields["create-task-title-input"]
        field.tap()
        field.typeText(title)
    }

    func setDescription(_ description: String) {
        let field = app.textFields["task-description-input"]
        field.tap()
        field.typeText(description)
    }

    func submit() {
        app.buttons["create-button"].tap()
    }
}
