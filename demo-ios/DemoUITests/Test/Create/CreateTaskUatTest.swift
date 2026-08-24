import XCTest

final class CreateTaskUatTest: UatTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.taskManagement }
    static var allureFeature: String { "Create task" }
    static var allureTmsLinks: [String] { ["100"] }

    func testShouldCreateTask() {
        // Given
        let context = TaskTestContext()
        steps.navigation.openMainPage()

        // When
        steps.tasks
            .openCreateTaskForm()
            .setTaskData(context.createTaskData())
            .submitForm()

        // Then
        validate.tasks.hasTask(context.title)

        // When
        steps.tasks.openTaskInfoForm(context.title)

        // Then
        validate.task.data(context.createTaskData())
    }
}
