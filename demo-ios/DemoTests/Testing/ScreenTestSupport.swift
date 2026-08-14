import SwiftUI
import ViewInspector
@testable import Demo

@MainActor
enum ScreenTestSupport {
    static func resetLocaleToEnglish() {
        AppLocale.shared.setLanguage(AppLocale.english)
    }

    static func host<V: View>(_ view: V) -> V {
        let appLocale = AppLocale.shared
        let hosted = view
            .environment(\.locale, Locale(identifier: appLocale.languageTag))
            .environment(\.appLocale, appLocale)
        ViewHostingTestSupport.host(hosted)
        return view
    }

    static func inspect<V: View>(_ view: V) throws -> InspectableView<ViewType.ClassifiedView> {
        let appLocale = AppLocale.shared
        return try view
            .environment(\.locale, Locale(identifier: appLocale.languageTag))
            .environment(\.appLocale, appLocale)
            .inspect(function: ViewHostingTestSupport.hostingKey)
    }

    static func expel() {
        ViewHostingTestSupport.expel()
    }
}
