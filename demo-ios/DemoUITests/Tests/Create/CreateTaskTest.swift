import XCTest

final class CreateTaskTest: MockedBackendTestBase {
    func testShouldCreateTaskWhenFormSubmittedWithValidData() throws {
        // Given
        let context = TaskTestContext()
        let task = context.stubTask()
        try mock.api()
            .createTask(task)
            .getTasks([task])
            .getTasks([task])
            .getTask(task)
            .getIsValid(true)

        let mainPage = MainPage(app: app)
        mainPage.waitUntilReady()

        // When
        mainPage.tapAddTask()
        let form = CreateTaskForm(app: app)
        form.waitUntilReady()
        form.setTitle(context.title)
        form.setDescription(context.description)
        form.submit()

        // Then
        mainPage.waitUntilReady()
        XCTAssertTrue(mainPage.hasTask(id: context.id, titled: context.title))
    }
}
