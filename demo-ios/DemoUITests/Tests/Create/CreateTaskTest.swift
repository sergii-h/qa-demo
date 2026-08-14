import XCTest

final class CreateTaskTest: MockedBackendTestBase {
    func testShouldCreateTaskWhenFormSubmittedWithValidData() throws {
        // Given
        let context = TaskTestContext()
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
        XCTAssertTrue(mainPage.hasTask(titled: context.title))
    }
}
