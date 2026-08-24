import XCTest

final class CreateTaskAccessibilityTest: AccessibilityTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.accessibility }
    static var allureFeature: String { "Create task" }
    static var allureTmsLinks: [String] { ["100"] }

    func testShouldHaveNoAccessibilityViolationsOnCreateTaskForm() {
        // Given
        steps.navigation.openMainPage()
        _ = steps.tasks.openCreateTaskForm()

        // When
        steps.accessibility.analyze(requiredIdentifiers: [
            "create-task-title-input",
            "task-description-input",
            "status-dropdown",
            "priority-dropdown",
            "create-button",
        ])

        // Then
        validate.accessibility.hasNoViolations()
    }
}
