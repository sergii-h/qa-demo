import SwiftUI

extension View {
    func testTag(_ identifier: String) -> some View {
        accessibilityIdentifier(identifier)
    }
}
