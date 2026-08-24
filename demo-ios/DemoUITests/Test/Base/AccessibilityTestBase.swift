import XCTest

class AccessibilityTestBase: MockedBackendHarness {
    override func setUpWithError() throws {
        try E2ESuite.skipUnless("accessibility")
        try super.setUpWithError()
        try startMockedBackend()
    }
}
