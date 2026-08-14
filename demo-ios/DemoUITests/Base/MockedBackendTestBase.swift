import XCTest

class ComposeInstrumentedTestBase: XCTestCase {
    let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
        app.launchEnvironment["API_BASE_URL"] = ProcessInfo.processInfo.environment["API_BASE_URL"]
            ?? "http://localhost:8085/v1/"
        app.launch()
    }
}

class MockedBackendTestBase: ComposeInstrumentedTestBase {}

class UatTestBase: ComposeInstrumentedTestBase {
    override func setUpWithError() throws {
        app.launchEnvironment["API_BASE_URL"] = "http://localhost:8080/v1/"
        try super.setUpWithError()
    }
}
