import XCTest

final class AccessibilityValidator {
    func hasNoViolations() {
        Allure.step("Validate no accessibility violations") {
            XCTAssertEqual(
                AccessibilityReport.violations,
                [],
                "Expected no accessibility violations, found: \(AccessibilityReport.violations.joined(separator: "; "))"
            )
        }
    }
}
