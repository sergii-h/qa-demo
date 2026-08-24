import XCTest

final class CreateTaskTest: MockedBackendTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.taskManagement }
    static var allureFeature: String { "Create task" }
    static var allureTmsLinks: [String] { ["100"] }

    private var context: TaskTestContext!
    private var response: TaskResponse!

    override func stubApis() throws {
        try super.stubApis()
        context = TaskTestContext()
        response = context.createTaskResponse()

        try support.mock.api()
            .createTask(response)
            .getTasks(response)
            .getTasks(response)
            .getTask(response)
            .getIsValid(true)
    }

    func testShouldCreateTaskWhenFormSubmittedWithValidData() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.tasks
            .openCreateTaskForm()
            .setTaskData(context.createTaskData())
            .submitForm()

        // Then
        validate.tasks.hasTask(response.title)

        // When
        steps.tasks.openTaskInfoForm(response.title)

        // Then
        validate.task.data(context.createTaskData())
    }
}
