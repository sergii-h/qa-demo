enum LanguageOption {
    case en
    case es

    var testTag: String {
        switch self {
        case .en: "language-option-en"
        case .es: "language-option-es"
        }
    }
}
