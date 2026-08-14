import XCTest

struct TaskTestContext {
    let title = "Task \(UUID().uuidString.prefix(8))"
    let description = "E2E description"
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

    func hasTask(titled title: String) -> Bool {
        app.staticTexts.matching(
            NSPredicate(format: "identifier BEGINSWITH 'task-title-' AND label == %@", title)
        ).firstMatch.waitForExistence(timeout: 10)
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
