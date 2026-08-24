import XCTest

final class LanguageSwitcherStep {
    private let languageSwitcher: LanguageSwitcherDropdown

    init(app: XCUIApplication) {
        languageSwitcher = LanguageSwitcherDropdown(app: app)
    }

    func selectLanguage(_ language: String) {
        Allure.step("Select language '\(language)'") {
            languageSwitcher.dropdown().tapWhenReady()
            switch language {
            case "ES":
                languageSwitcher.spanishOption().tapWhenReady(timeout: 5)
            case "EN":
                languageSwitcher.englishOption().tapWhenReady(timeout: 5)
            default:
                XCTFail("Unsupported language: \(language)")
            }
        }
    }
}
