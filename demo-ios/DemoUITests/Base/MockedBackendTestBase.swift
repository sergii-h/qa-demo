import XCTest

class ComposeInstrumentedTestBase: XCTestCase {
    let app = XCUIApplication()

    override func setUpWithError() throws {
        continueAfterFailure = false
        if app.launchEnvironment["API_BASE_URL"] == nil {
            app.launchEnvironment["API_BASE_URL"] = ProcessInfo.processInfo.environment["API_BASE_URL"]
                ?? "http://localhost:8085/v1/"
        }
    }
}

class MockedBackendTestBase: ComposeInstrumentedTestBase {
    let mock = ApiRouteMockClient()

    override func setUpWithError() throws {
        try super.setUpWithError()
        try mock.start()
        app.launch()
    }

    override func tearDownWithError() throws {
        try mock.reset()
        try super.tearDownWithError()
    }
}

class UatTestBase: ComposeInstrumentedTestBase {
    override func setUpWithError() throws {
        app.launchEnvironment["API_BASE_URL"] = "http://localhost:8080/v1/"
        try super.setUpWithError()
        app.launch()
    }
}
