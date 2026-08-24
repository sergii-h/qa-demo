import XCTest

final class NavigationStep {
    private let mainPage: MainPage

    init(app: XCUIApplication) {
        mainPage = MainPage(app: app)
    }

    func openMainPage() {
        Allure.step("Open main page") {
            mainPage.waitUntilReady()
            mainPage.createTaskButton().waitUntilExists()
        }
    }

    func refreshMainPage() {
        Allure.step("Refresh main page") {
            mainPage.pullToRefresh()
            mainPage.waitUntilRefreshComplete()
        }
    }
}
