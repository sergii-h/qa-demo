import XCTest

final class ValidationProvider {
    let tasks: TasksValidator
    let task: TaskValidator
    let language: LanguageValidator
    let accessibility = AccessibilityValidator()

    init(app: XCUIApplication) {
        tasks = TasksValidator(app: app)
        task = TaskValidator(app: app)
        language = LanguageValidator(app: app)
    }
}
