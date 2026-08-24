import XCTest

final class LanguageValidator {
    private let mainPage: MainPage

    init(app: XCUIApplication) {
        mainPage = MainPage(app: app)
    }

    func uiIsInSpanish() {
        Allure.step("Validate table headers and buttons are in Spanish") {
            mainPage.pageTitle().waitUntilExists()
            XCTAssertEqual(mainPage.pageTitle().label, "Tareas")
            XCTAssertEqual(mainPage.createTaskButton().label, "Crear tarea")
        }
    }

    func statusTagShowsText(_ status: TaskStatus, expectedText: String) {
        Allure.step("Validate status tag for '\(status.rawValue)' shows '\(expectedText)'") {
            mainPage.statusTag(status).waitUntilExists()
            XCTAssertEqual(mainPage.statusTag(status).label, expectedText)
        }
    }

    func priorityTagShowsText(_ priority: TaskPriority, expectedText: String) {
        Allure.step("Validate priority tag for '\(priority.rawValue)' shows '\(expectedText)'") {
            mainPage.priorityTag(priority).waitUntilExists()
            XCTAssertEqual(mainPage.priorityTag(priority).label, expectedText)
        }
    }
}
