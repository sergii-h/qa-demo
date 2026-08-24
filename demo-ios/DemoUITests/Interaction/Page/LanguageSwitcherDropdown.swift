import XCTest

final class LanguageSwitcherDropdown: XCUIPage {
    func dropdown() -> XCUIElement {
        app.buttons["language-switcher"]
    }

    func englishOption() -> XCUIElement {
        app.buttons["language-option-en"]
    }

    func spanishOption() -> XCUIElement {
        app.buttons["language-option-es"]
    }
}
