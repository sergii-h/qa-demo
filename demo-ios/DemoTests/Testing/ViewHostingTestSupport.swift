import SwiftUI
import ViewInspector

@MainActor
enum ViewHostingTestSupport {
    static let hostingKey = "DemoTests.hostedView"

    static func expel() {
        ViewHosting.expel(function: hostingKey)
        ViewHosting.expel(function: "launchApp(initialPath:)")
        ViewHosting.expel(function: "host(_:)")
    }

    static func host<V: View>(_ view: V) {
        ViewHosting.expel(function: hostingKey)
        ViewHosting.host(view: view, function: hostingKey)
    }
}
