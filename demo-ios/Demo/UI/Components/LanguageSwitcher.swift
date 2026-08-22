import SwiftUI

struct LanguageSwitcher: View {
    private var locale: AppLocale { AppLocale.shared }

    var body: some View {
        Menu {
            Button {
                locale.setLanguage(AppLocale.english)
            } label: {
                Text("EN")
            }
            .testTag("language-option-en")

            Button {
                locale.setLanguage(AppLocale.spanish)
            } label: {
                Text("ES")
            }
            .testTag("language-option-es")
        } label: {
            Text(locale.languageTag == AppLocale.spanish ? "ES" : "EN")
                .font(.body.weight(.semibold))
                .foregroundStyle(TaskColors.primary)
        }
        .testTag("language-switcher")
        .accessibilityLabel(locale.localizedString("language_label"))
    }
}
