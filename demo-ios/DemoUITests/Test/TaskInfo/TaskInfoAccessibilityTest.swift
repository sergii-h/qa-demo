import XCTest

final class TaskInfoAccessibilityTest: AccessibilityTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.accessibility }
    static var allureFeature: String { "View task info" }
    static var allureTmsLinks: [String] { ["102"] }

    private var context: TaskTestContext!
    private var response: TaskResponse!

    override func stubApis() throws {
        context = TaskTestContext()
        response = context.createTaskResponse()

        try support.mock.api()
            .getTasks(response)
            .getTask(response)
            .getIsValid(true)
    }

    func testShouldHaveNoAccessibilityViolationsOnTaskInfoForm() {
        // Given
        steps.navigation.openMainPage()
        steps.tasks.openTaskInfoForm(response.title)

        // When
        steps.accessibility.analyze(requiredIdentifiers: [
            "description",
            "detail-description-label",
            "detail-validated-label",
            "valid",
        ])

        // Then
        validate.accessibility.hasNoViolations()
    }
}
