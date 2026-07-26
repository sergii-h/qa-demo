@accessibility @task-table
Feature: Task table accessibility

  Background:
    Given API mocks return a task list with two tasks

  Scenario: Should have no accessibility violations on task table
    Given the user is on the main page
    When the page is analyzed for accessibility
    Then there are no accessibility violations
