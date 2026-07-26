@edit-task
Feature: Edit task

  Background:
    Given API mocks are set up for editing a task

  Scenario: Should edit task through complete UI workflow
    Given the user is on the main page
    When the user edits the created task through the UI
    And the user opens task info for the updated task
    Then the updated task details are displayed
