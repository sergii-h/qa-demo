import Foundation

enum AppConfiguration {
    static var apiBaseURL: URL {
        if let override = ProcessInfo.processInfo.environment["API_BASE_URL"],
           let url = URL(string: override) {
            return url
        }
        if let configured = Bundle.main.object(forInfoDictionaryKey: "APIBaseURL") as? String,
           let url = URL(string: configured) {
            return url
        }
        return URL(string: "http://localhost:8080/v1/")!
    }

    static var wiremockURL: URL {
        if let override = ProcessInfo.processInfo.environment["WIREMOCK_URL"],
           let url = URL(string: override) {
            return url
        }
        return URL(string: "http://localhost:8085")!
    }
}
