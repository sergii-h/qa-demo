import XCTest

class UatTestBase: XCUITestBase {
    override func setUpWithError() throws {
        try E2ESuite.skipUnless("uat")
        try super.setUpWithError()
        launchApp(
            apiBaseURL: ProcessInfo.processInfo.environment["API_BASE_URL"] ?? "http://localhost:8080/v1/"
        )
        MainPage(app: app).waitUntilReady()
    }
}
