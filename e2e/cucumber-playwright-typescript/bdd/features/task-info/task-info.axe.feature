@accessibility @task-info
Feature: View task info accessibility

  Background:
    Given API mocks are set up for viewing task info

  Scenario: Should have no accessibility violations on task info modal
    Given the user is on the main page
    When the user opens task info for the created task
    And the page is analyzed for accessibility
    Then there are no accessibility violations
