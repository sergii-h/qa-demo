import XCTest

final class TasksValidator {
    private let mainPage: MainPage

    init(app: XCUIApplication) {
        mainPage = MainPage(app: app)
    }

    func hasTask(_ title: String) {
        Allure.step("Validate task list has task '\(title)'") {
            let normalizedTitle = title.trimmingCharacters(in: .whitespacesAndNewlines)
            mainPage.waitUntilTaskWithTitlePresent(normalizedTitle)
            let taskTitle = mainPage.taskTitleByTitle(normalizedTitle)
            XCTAssertTrue(taskTitle.exists)
            XCTAssertEqual(taskTitle.label, normalizedTitle)
        }
    }

    func hasNoTask(_ title: String) {
        Allure.step("Validate task '\(title)' is removed from list") {
            mainPage.waitUntilTaskWithTitleAbsent(title.trimmingCharacters(in: .whitespacesAndNewlines))
            XCTAssertFalse(mainPage.taskTitleByTitle(title).exists)
        }
    }
}
