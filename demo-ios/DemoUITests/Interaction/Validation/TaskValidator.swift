import XCTest

final class TaskValidator {
    private let infoTaskModal: InfoTaskModal

    init(app: XCUIApplication) {
        infoTaskModal = InfoTaskModal(app: app)
    }

    @discardableResult
    func data(_ taskData: TaskData) -> TaskValidator {
        Allure.step("Validate task info data") {
            infoTaskModal.title().waitUntilExists()
            XCTAssertEqual(infoTaskModal.title().label, taskData.title)
            XCTAssertEqual(infoTaskModal.descriptionField().label, taskData.description)
            XCTAssertTrue(infoTaskModal.statusTag(taskData.status).exists)
            XCTAssertTrue(infoTaskModal.priorityTag(taskData.priority).exists)
        }
        return self
    }

    func isValid() {
        Allure.step("Validate task is marked as valid") {
            infoTaskModal.validIcon().waitUntilExists()
        }
    }
}
