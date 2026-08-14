import Foundation
import SwiftUI

@Observable
final class AppLocale {
    static let shared = AppLocale()

    static let english = "en"
    static let spanish = "es"

    private let defaultsKey = "language_tag"
    private let defaults = UserDefaults.standard

    private(set) var languageTag: String

    private init() {
        languageTag = defaults.string(forKey: defaultsKey) ?? Self.defaultLanguageTag()
    }

    func initialize() {
        languageTag = defaults.string(forKey: defaultsKey) ?? Self.defaultLanguageTag()
    }

    static func defaultLanguageTag() -> String {
        let language = Locale.current.language.languageCode?.identifier ?? AppLocale.english
        return language.hasPrefix(spanish) ? spanish : english
    }

    func setLanguage(_ tag: String) {
        guard languageTag != tag else { return }
        defaults.set(tag, forKey: defaultsKey)
        languageTag = tag
    }

    func localizedString(_ key: String.LocalizationValue) -> String {
        String(localized: LocalizedStringResource(key, locale: Locale(identifier: languageTag)))
    }

    func localizedString(_ key: String.LocalizationValue, _ arguments: CVarArg...) -> String {
        let format = String(localized: LocalizedStringResource(key, locale: Locale(identifier: languageTag)))
        return String(format: format, locale: Locale(identifier: languageTag), arguments: arguments)
    }
}

private struct AppLocaleKey: EnvironmentKey {
    static let defaultValue = AppLocale.shared
}

extension EnvironmentValues {
    var appLocale: AppLocale {
        get { self[AppLocaleKey.self] }
        set { self[AppLocaleKey.self] = newValue }
    }
}
