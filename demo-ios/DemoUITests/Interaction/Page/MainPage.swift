import XCTest

final class MainPage: XCUIPage {
    func createTaskButton() -> XCUIElement {
        element("add-task-button")
    }

    func pageTitle() -> XCUIElement {
        element("page-title")
    }

    func taskTitle(taskId: String) -> XCUIElement {
        element("task-title-\(taskId)")
    }

    func taskTitleByTitle(_ title: String) -> XCUIElement {
        let normalized = title.trimmingCharacters(in: .whitespacesAndNewlines)
        return app.descendants(matching: .any).matching(
            NSPredicate(
                format: "identifier BEGINSWITH %@ AND (label == %@ OR value == %@)",
                "task-title-",
                normalized,
                normalized
            )
        ).firstMatch
    }

    func infoButton(taskId: String) -> XCUIElement {
        element("info-button-\(taskId)")
    }

    func editButton(taskId: String) -> XCUIElement {
        element("edit-button-\(taskId)")
    }

    func deleteButton(taskId: String) -> XCUIElement {
        element("delete-button-\(taskId)")
    }

    func statusTag(_ status: TaskStatus) -> XCUIElement {
        element("status-tag-\(status.rawValue)")
    }

    func priorityTag(_ priority: TaskPriority) -> XCUIElement {
        element("priority-tag-\(priority.rawValue)")
    }

    func waitUntilReady() {
        waitUntilAbsent("loading-spinner")
        waitUntilPresent("add-task-button")
    }

    func waitUntilTaskWithTitlePresent(_ title: String, timeout: TimeInterval = 10) {
        taskTitleByTitle(title).waitUntilExists(timeout: timeout)
    }

    func waitUntilTaskWithTitleAbsent(_ title: String, timeout: TimeInterval = 10) {
        taskTitleByTitle(title).waitUntilGone(timeout: timeout)
    }

    func waitUntilCreateTaskButtonPresent() {
        waitUntilPresent("add-task-button")
    }

    func waitUntilLoadingSpinnerAbsent() {
        waitUntilAbsent("loading-spinner")
    }

    func pullToRefresh() {
        waitUntilPresent("task-list")
        let list = app.scrollViews["task-list"].exists ? app.scrollViews["task-list"] : element("task-list")
        let start = list.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.05))
        let finish = list.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.55))
        start.press(forDuration: 0.05, thenDragTo: finish)
    }

    func waitUntilRefreshComplete(timeout: TimeInterval = 15) {
        let refreshing = element("refreshing")
        if refreshing.waitForExistence(timeout: 2) {
            waitUntilAbsent("refreshing", timeout: timeout)
        }
        waitUntilReady()
    }
}
