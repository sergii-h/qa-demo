import XCTest

class XCUIPage {
    let app: XCUIApplication

    init(app: XCUIApplication) {
        self.app = app
    }

    func element(_ identifier: String) -> XCUIElement {
        app.descendants(matching: .any)[identifier]
    }

    func button(_ identifier: String) -> XCUIElement {
        app.buttons[identifier]
    }

    func waitUntilPresent(_ identifier: String, timeout: TimeInterval = 10) {
        element(identifier).waitUntilExists(timeout: timeout)
    }

    func waitUntilAbsent(_ identifier: String, timeout: TimeInterval = 10) {
        element(identifier).waitUntilGone(timeout: timeout)
    }

    func tap(_ identifier: String) {
        dismissKeyboardIfPresent()
        let target = app.buttons[identifier].exists ? app.buttons[identifier] : element(identifier)
        target.tapWhenReady()
    }

    func typeText(_ identifier: String, _ text: String) {
        let field = inputField(identifier)
        field.tapWhenReady()
        field.typeText(text)
    }

    func replaceText(_ identifier: String, with text: String) {
        let field = inputField(identifier)
        field.tapWhenReady()
        if let current = field.value as? String, !current.isEmpty {
            field.typeText(String(repeating: XCUIKeyboardKey.delete.rawValue, count: current.count))
        }
        field.typeText(text)
    }

    func selectOption(dropdown: XCUIElement, option: XCUIElement) {
        dismissKeyboardIfPresent()
        dropdown.tapWhenReady()
        app.descendants(matching: .any)
            .matching(identifier: option.identifier)
            .firstMatch
            .tapWhenReady(timeout: 10)
        if !dropdown.waitForExistence(timeout: 2) {
            app.buttons["BackButton"].tapWhenReady()
        }
        dropdown.waitUntilExists(timeout: 10)
    }

    private func dismissKeyboardIfPresent() {
        let keyboard = app.keyboards.firstMatch
        guard keyboard.exists else { return }

        let titleField = app.textFields.matching(
            NSPredicate(format: "identifier ENDSWITH %@", "title-input")
        ).firstMatch
        if titleField.exists {
            titleField.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
            _ = XCTWaiter().wait(
                for: [
                    XCTNSPredicateExpectation(
                        predicate: NSPredicate(format: "hasKeyboardFocus == 1"),
                        object: titleField
                    )
                ],
                timeout: 2
            )
            let returnKey = app.keyboards.buttons["Return"]
            if returnKey.exists {
                returnKey.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
            }
        }

        _ = XCTWaiter().wait(
            for: [
                XCTNSPredicateExpectation(
                    predicate: NSPredicate(format: "exists == false"),
                    object: app.keyboards.firstMatch
                )
            ],
            timeout: 3
        )
        XCTAssertFalse(
            app.keyboards.firstMatch.exists,
            "Keyboard still visible after dismiss"
        )
    }

    private func inputField(_ identifier: String) -> XCUIElement {
        let textField = app.textFields[identifier]
        if textField.exists {
            return textField
        }
        let textView = app.textViews[identifier]
        if textView.exists {
            return textView
        }
        return element(identifier)
    }
}

extension XCUIElement {
    func waitUntilExists(
        timeout: TimeInterval = 10,
        file: StaticString = #file,
        line: UInt = #line
    ) {
        XCTAssertTrue(
            waitForExistence(timeout: timeout),
            "Element '\(identifier)' did not appear within \(timeout)s",
            file: file,
            line: line
        )
    }

    func waitUntilGone(
        timeout: TimeInterval = 10,
        file: StaticString = #file,
        line: UInt = #line
    ) {
        let expectation = XCTNSPredicateExpectation(
            predicate: NSPredicate(format: "exists == false"),
            object: self
        )
        let result = XCTWaiter().wait(for: [expectation], timeout: timeout)
        XCTAssertEqual(
            result,
            .completed,
            "Element '\(identifier)' was still present after \(timeout)s",
            file: file,
            line: line
        )
    }

    func tapWhenReady(
        timeout: TimeInterval = 10,
        file: StaticString = #file,
        line: UInt = #line
    ) {
        waitUntilExists(timeout: timeout, file: file, line: line)
        let frame = self.frame
        XCTAssertTrue(
            frame.width.isFinite && frame.height.isFinite && frame.width > 0 && frame.height > 0,
            "Element '\(identifier)' is not tappable (frame: \(frame))",
            file: file,
            line: line
        )
        coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
    }
}
