import SwiftUI

enum TaskColors {
    static let background = Color(red: 0.96, green: 0.96, blue: 0.96)
    static let surface = Color.white
    static let primary = Color(red: 0.09, green: 0.40, blue: 0.75)

    static let todoBlue = Color(red: 0.13, green: 0.59, blue: 0.95)
    static let inProgressOrange = Color(red: 1.0, green: 0.60, blue: 0.0)
    static let doneGreen = Color(red: 0.30, green: 0.69, blue: 0.31)

    static let lowGreen = Color(red: 0.40, green: 0.73, blue: 0.42)
    static let mediumOrange = Color(red: 1.0, green: 0.65, blue: 0.15)
    static let highRed = Color(red: 0.94, green: 0.33, blue: 0.31)
}

enum DemoTheme {
    static let accent = TaskColors.primary
}

struct DemoAppTheme<Content: View>: View {
    @ViewBuilder let content: () -> Content

    var body: some View {
        content()
            .tint(DemoTheme.accent)
    }
}
