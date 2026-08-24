import XCTest

final class StepProvider {
    let tasks: TaskTableStep
    let language: LanguageSwitcherStep
    let navigation: NavigationStep
    let accessibility: AccessibilityStep

    init(app: XCUIApplication) {
        tasks = TaskTableStep(app: app)
        language = LanguageSwitcherStep(app: app)
        navigation = NavigationStep(app: app)
        accessibility = AccessibilityStep(app: app)
    }
}
