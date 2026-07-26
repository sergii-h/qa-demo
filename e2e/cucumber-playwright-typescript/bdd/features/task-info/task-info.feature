@task-info
Feature: View task info

  Background:
    Given API mocks are set up for viewing task info

  Scenario: Should view task detail through UI
    Given the user is on the main page
    When the user opens task info for the created task
    Then the task details are displayed
    And the task is marked as valid
