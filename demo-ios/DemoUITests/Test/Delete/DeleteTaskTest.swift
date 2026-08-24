import XCTest

final class DeleteTaskTest: MockedBackendTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.taskManagement }
    static var allureFeature: String { "Delete task" }
    static var allureTmsLinks: [String] { ["99", "115"] }

    private var context: TaskTestContext!
    private var response: TaskResponse!

    override func stubApis() throws {
        context = TaskTestContext()
        response = context.createTaskResponse()

        try support.mock.api()
            .getTasks(response)
            .deleteTask()
    }

    func testShouldRemoveTaskFromListWhenDeleteSucceeds() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.tasks.deleteTask(response.title)

        // Then
        validate.tasks.hasNoTask(response.title)
    }
}
