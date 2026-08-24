final class ApiRouteMockClient {
    private let wireMock = WireMockClient()
    private var apiMock: ApiRouteMock?

    func api() -> ApiRouteMock {
        if apiMock == nil {
            apiMock = ApiRouteMock(wireMock: wireMock)
        }
        return apiMock!
    }

    func start() throws {
        try reset()
    }

    func reset() throws {
        try wireMock.reset()
        apiMock = nil
    }
}
