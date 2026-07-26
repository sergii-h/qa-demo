@delete-task
Feature: Delete task

  Background:
    Given API mocks are set up for deleting a task

  Scenario: Should delete task through UI and verify removal from list
    Given the user is on the main page
    When the user deletes the created task
    Then the task is removed from the list
