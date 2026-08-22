import Foundation
import Testing
@testable import Demo

struct AppConfigurationTests {
    @Test
    func shouldReturnDefaultApiBaseURLWhenNoOverride() {
        // When
        let url = AppConfiguration.apiBaseURL

        // Then
        #expect(url.absoluteString == "http://localhost:8080/v1/")
    }

    @Test
    func shouldReturnDefaultWiremockURLWhenNoOverride() {
        // When
        let url = AppConfiguration.wiremockURL

        // Then
        #expect(url.absoluteString == "http://localhost:8085")
    }
}
