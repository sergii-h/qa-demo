@accessibility @create-task
Feature: Create task accessibility

  Scenario: Should have no accessibility violations on create task form
    Given the user is on the main page
    When the user opens the create task form
    And the page is analyzed for accessibility
    Then there are no accessibility violations
