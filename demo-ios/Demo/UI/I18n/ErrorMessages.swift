import Foundation

enum ErrorMessages {
    static func mapTaskError(_ error: Error, locale: AppLocale) -> String {
        if let apiError = error as? APIErrorResponse {
            if let message = parseErrorBody(apiError.body?.data(using: .utf8)), !message.isEmpty {
                return message
            }
            switch apiError.statusCode {
            case 400:
                return locale.localizedString("error_invalid_task_data")
            case 404:
                return locale.localizedString("error_task_not_found")
            case 409:
                return locale.localizedString("error_title_already_exists")
            default:
                return locale.localizedString("error_request_failed", apiError.statusCode)
            }
        }
        return error.localizedDescription.isEmpty
            ? locale.localizedString("error_something_went_wrong")
            : error.localizedDescription
    }

    static func isDuplicateTitleError(_ error: Error) -> Bool {
        (error as? APIErrorResponse)?.statusCode == 409
    }
}
