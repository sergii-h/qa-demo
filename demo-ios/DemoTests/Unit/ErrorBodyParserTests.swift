import Foundation
import Testing
@testable import Demo

struct ErrorBodyParserTests {
    @Test
    func shouldReturnMessageWhenErrorBodyContainsMessage() {
        // Given
        let data = Data("{\"message\":\"Duplicate title\"}".utf8)

        // When
        let message = parseErrorBody(data)

        // Then
        #expect(message == "Duplicate title")
    }

    @Test
    func shouldReturnNilWhenDataIsEmpty() {
        // Given
        // When
        let message = parseErrorBody(Data())

        // Then
        #expect(message == nil)
    }

    @Test
    func shouldReturnNilWhenDataIsNil() {
        // When
        let message = parseErrorBody(nil)

        // Then
        #expect(message == nil)
    }
}
