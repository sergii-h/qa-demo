import XCTest

final class TaskInfoTest: MockedBackendTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.taskManagement }
    static var allureFeature: String { "View task info" }
    static var allureTmsLinks: [String] { ["102", "115"] }

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

    func testShouldDisplayTaskDetailsWhenInfoOpened() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.tasks.openTaskInfoForm(response.title)

        // Then
        validate.task
            .data(context.createTaskData())
            .isValid()
    }
}
