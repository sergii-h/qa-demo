import XCTest

final class EditTaskTest: MockedBackendTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.taskManagement }
    static var allureFeature: String { "Edit task" }
    static var allureTmsLinks: [String] { ["101", "115"] }

    private var context: TaskTestContext!
    private var response: TaskResponse!

    override func stubApis() throws {
        context = TaskTestContext(status: .todo, priority: .medium)
        response = context.createTaskResponse()

        try support.mock.api()
            .getTasks(response)
            .getTask(response)
            .getIsValid(true)
    }

    func testShouldUpdateTaskWhenEditFormSubmitted() throws {
        // Given
        let updatedContext = context.copy(
            title: "\(context.title)-Updated",
            description: "\(context.description)-Updated",
            status: .inProgress,
            priority: .high
        )
        let updatedResponse = updatedContext.createTaskResponse()

        steps.navigation.openMainPage()

        // When
        let editTaskStep = steps.tasks
            .openTaskEditForm(response.title)
            .setTaskData(updatedContext.createTaskData())

        try support.mock.api()
            .updateTask(updatedResponse)
            .getTasks(updatedResponse)
            .getTasks(updatedResponse)
            .getTask(updatedResponse)
            .getIsValid(true)

        _ = editTaskStep.submitForm()
        steps.tasks.openTaskInfoForm(updatedContext.title)

        // Then
        validate.task.data(updatedContext.createTaskData())
    }
}
