@uat @create-task
Feature: Create task UAT

  Scenario: Should create task
    Given the user is on the main page
    When the user creates a task through the UI
    Then the task appears in the list
    When the user opens task info for the created task
    Then the task details are displayed
