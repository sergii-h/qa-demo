import SwiftUI
import ViewInspector

@MainActor
enum ViewHostingTestSupport {
    static let hostingKey = "DemoTests.hostedView"
    private static var generation = 0

    static func expel() {
        generation += 1
        ViewHosting.expel(function: hostingKey)
        ViewHosting.expel(function: "launchApp(initialPath:)")
        ViewHosting.expel(function: "host(_:)")
    }

    static func host<V: View>(_ view: V) -> Int {
        generation += 1
        let token = generation
        ViewHosting.expel(function: hostingKey)
        ViewHosting.host(view: view, function: hostingKey)
        return token
    }

    static func expel(token: Int) {
        guard token == generation else { return }
        expel()
    }
}
