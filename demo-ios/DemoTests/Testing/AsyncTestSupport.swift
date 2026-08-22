import Foundation

@MainActor
enum AsyncTestSupport {
    static func waitUntil(
        timeoutMilliseconds: Int = 1000,
        intervalMilliseconds: Int = 20,
        _ condition: () -> Bool
    ) async {
        let iterations = max(1, timeoutMilliseconds / intervalMilliseconds)
        for _ in 0..<iterations {
            if condition() { return }
            try? await _Concurrency.Task.sleep(for: .milliseconds(intervalMilliseconds))
        }
    }
}
