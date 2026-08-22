import Foundation

enum TaskDateFormatter {
    static func format(_ isoString: String, locale: AppLocale) -> String {
        guard let date = parseISODate(isoString) else { return isoString }
        let formatter = DateFormatter()
        formatter.locale = Locale(identifier: locale.languageTag)
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }

    private static func parseISODate(_ value: String) -> Date? {
        let withFractionalSeconds = ISO8601DateFormatter()
        withFractionalSeconds.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        if let date = withFractionalSeconds.date(from: value) {
            return date
        }
        let withoutFractionalSeconds = ISO8601DateFormatter()
        withoutFractionalSeconds.formatOptions = [.withInternetDateTime]
        return withoutFractionalSeconds.date(from: value)
    }
}
