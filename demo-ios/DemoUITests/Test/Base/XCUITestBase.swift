import XCTest

enum E2ESuite {
    static var current: String {
        ProcessInfo.processInfo.environment["E2E_SUITE"] ?? "e2e"
    }

    static func skipUnless(_ suite: String) throws {
        if current != suite {
            throw XCTSkip("Skipping \(suite) tests because E2E_SUITE=\(current)")
        }
    }
}

class XCUITestBase: XCTestCase {
    let app = XCUIApplication()
    lazy var support = SupportProvider()
    lazy var steps = StepProvider(app: app)
    lazy var validate = ValidationProvider(app: app)

    override func setUpWithError() throws {
        try super.setUpWithError()
        continueAfterFailure = false
        if let annotated = type(of: self) as? AllureAnnotated.Type {
            Allure.annotate(
                epic: annotated.allureEpic,
                feature: annotated.allureFeature,
                tms: annotated.allureTmsLinks
            )
        }
    }

    override func tearDownWithError() throws {
        app.terminate()
        try super.tearDownWithError()
    }

    func launchApp(apiBaseURL: String) {
        app.launchArguments = ["-language_tag", "en"]
        app.launchEnvironment["API_BASE_URL"] = apiBaseURL
        app.launch()
    }
}

class MockedBackendHarness: XCUITestBase {
    func stubApis() throws {
        try support.mock.api().getTasks()
    }

    func startMockedBackend() throws {
        try support.mock.start()
        try stubApis()
        launchApp(
            apiBaseURL: ProcessInfo.processInfo.environment["API_BASE_URL"] ?? "http://localhost:8085/v1/"
        )
        MainPage(app: app).waitUntilReady()
    }

    override func tearDownWithError() throws {
        try support.mock.reset()
        try super.tearDownWithError()
    }
}

class MockedBackendTestBase: MockedBackendHarness {
    override func setUpWithError() throws {
        try E2ESuite.skipUnless("e2e")
        try super.setUpWithError()
        try startMockedBackend()
    }
}
