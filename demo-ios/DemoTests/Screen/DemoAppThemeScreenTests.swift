import SwiftUI
import ViewInspector
import Testing
@testable import Demo
@MainActor
extension DemoAppTheme: Inspectable {}
@MainActor
@Suite(.serialized)
struct DemoAppThemeScreenTests {
    @Test
    func shouldRenderChildContentWhenHostedInTheme() async throws {
        // Given
        let view = ScreenTestSupport.host(
            DemoAppTheme {
                Text("Themed content").testTag("themed-content")
            }
        )
        defer { ScreenTestSupport.expel() }
        // Then
        let label = try view.inspect().find(viewWithAccessibilityIdentifier: "themed-content")
        #expect(try label.text().string() == "Themed content")
    }
}
