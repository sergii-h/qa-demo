@create-task
Feature: Create task

  Background:
    Given API mocks are set up for creating a task

  Scenario: Should create task through complete UI workflow
    Given the user is on the main page
    When the user creates a task through the UI
    Then the task appears in the list
    When the user opens task info for the created task
    Then the task details are displayed
