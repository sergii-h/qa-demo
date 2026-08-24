import XCTest

final class AccessibilityReport {
    static var violations: [String] = []
}

final class AccessibilityStep {
    private let app: XCUIApplication

    init(app: XCUIApplication) {
        self.app = app
    }

    func analyze(requiredIdentifiers: [String]) {
        Allure.step("Analyze page accessibility") {
            AccessibilityReport.violations = requiredIdentifiers.compactMap { identifier in
                let element = app.descendants(matching: .any)[identifier]
                if element.waitForExistence(timeout: 5) {
                    return nil
                }
                return "Missing element with accessibility identifier '\(identifier)'"
            }
        }
    }
}
