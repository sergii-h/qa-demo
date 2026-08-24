import XCTest

final class LanguageSupportTest: MockedBackendTestBase, AllureAnnotated {
    static var allureEpic: String { AllureEpic.translation }
    static var allureFeature: String { "Language support" }
    static var allureTmsLinks: [String] { ["104"] }

    private var testContext: TaskTestContext!

    override func stubApis() throws {
        testContext = TaskTestContext(status: .todo, priority: .low)
        let response = testContext.createTaskResponse()

        try support.mock.api()
            .getTasks(response)
            .getTasks(response)
    }

    func testShouldSwitchUiToSpanishWhenEsSelected() {
        // Given
        steps.navigation.openMainPage()

        // When
        steps.language.selectLanguage("ES")

        // Then
        validate.language.uiIsInSpanish()
        validate.language.statusTagShowsText(.todo, expectedText: "Por hacer")
        validate.language.priorityTagShowsText(.low, expectedText: "Baja")
    }
}
