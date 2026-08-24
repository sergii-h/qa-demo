import XCTest

protocol AllureAnnotated: AnyObject {
    static var allureEpic: String { get }
    static var allureFeature: String { get }
    static var allureTmsLinks: [String] { get }
}

enum Allure {
    static func annotate(epic: String, feature: String, tms: [String]) {
        XCTContext.runActivity(named: "allure.epic:\(epic)") { _ in }
        XCTContext.runActivity(named: "allure.feature:\(feature)") { _ in }
        for id in tms {
            XCTContext.runActivity(named: "allure.tms:\(id)") { _ in }
        }
    }

    static func step(_ name: String, _ body: () -> Void) {
        XCTContext.runActivity(named: name) { _ in
            body()
        }
    }
}
