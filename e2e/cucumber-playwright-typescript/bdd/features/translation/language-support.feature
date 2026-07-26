@translation
Feature: Language support

  Background:
    Given API mocks are set up for language support

  Scenario: Should switch all UI text and status/priority tag values to Spanish when ES is selected
    Given the user is on the main page
    When the user selects Spanish language
    Then the UI is displayed in Spanish
    And the status tag shows Spanish text for TODO
    And the priority tag shows Spanish text for LOW
