import Foundation

enum APIError: Error, Equatable {
    case invalidURL
    case invalidResponse
    case httpError(statusCode: Int, body: String?)
    case decodingFailed
}

struct APIErrorResponse: Error {
    let statusCode: Int
    let body: String?

    var localizedDescription: String {
        "HTTP \(statusCode)"
    }
}

extension APIErrorResponse: Equatable {
    static func == (lhs: APIErrorResponse, rhs: APIErrorResponse) -> Bool {
        lhs.statusCode == rhs.statusCode && lhs.body == rhs.body
    }
}

func parseErrorBody(_ data: Data?) -> String? {
    guard let data, !data.isEmpty else { return nil }
    return (try? JSONDecoder().decode(ErrorResponse.self, from: data))?.message
}
