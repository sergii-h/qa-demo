import Foundation
@testable import Demo

final class StubURLProtocol: URLProtocol, @unchecked Sendable {
    nonisolated(unsafe) static var requestHandler: ((URLRequest) throws -> (HTTPURLResponse, Data))?
    nonisolated(unsafe) private static var installedOwner: ObjectIdentifier?

    static func install(
        owner: ObjectIdentifier,
        handler: @escaping (URLRequest) throws -> (HTTPURLResponse, Data)
    ) {
        installedOwner = owner
        requestHandler = handler
    }

    static func uninstall(owner: ObjectIdentifier) {
        guard installedOwner == owner else { return }
        requestHandler = nil
        installedOwner = nil
    }

    override class func canInit(with request: URLRequest) -> Bool {
        request.url?.host == "stub.test"
    }
    override class func canonicalRequest(for request: URLRequest) -> URLRequest { request }

    override func startLoading() {
        guard let handler = Self.requestHandler else {
            client?.urlProtocol(self, didFailWithError: APIError.invalidResponse)
            return
        }
        do {
            let (response, data) = try handler(request)
            client?.urlProtocol(self, didReceive: response, cacheStoragePolicy: .notAllowed)
            client?.urlProtocol(self, didLoad: data)
            client?.urlProtocolDidFinishLoading(self)
        } catch {
            client?.urlProtocol(self, didFailWithError: error)
        }
    }

    override func stopLoading() {}
}

enum StubURLSessionFactory {
    static func makeSession() -> URLSession {
        let configuration = URLSessionConfiguration.ephemeral
        configuration.protocolClasses = [StubURLProtocol.self]
        return URLSession(configuration: configuration)
    }
}
