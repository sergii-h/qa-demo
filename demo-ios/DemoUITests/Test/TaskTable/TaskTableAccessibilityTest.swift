import XCTest

final class TaskTableAccessibilityTest: AccessibilityTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.accessibility }
    static var allureFeature: String { "Task table" }
    static var allureTmsLinks: [String] { ["98"] }

    override func stubApis() throws {
        try support.mock.api().getTasks(
            TaskTestContext().createTaskResponse(),
            TaskTestContext().createTaskResponse()
        )
    }

    func testShouldHaveNoAccessibilityViolationsOnTaskTableWhenTasksLoaded() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.accessibility.analyze(requiredIdentifiers: [
            "page-title",
            "task-list",
            "add-task-button",
        ])

        // Then
        validate.accessibility.hasNoViolations()
    }
}
