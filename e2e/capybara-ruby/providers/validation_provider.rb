# frozen_string_literal: true

require_relative '../interactions/validators/accessibility_validator'
require_relative '../interactions/validators/language_validator'
require_relative '../interactions/validators/task_validator'
require_relative '../interactions/validators/tasks_validator'

class ValidationProvider
  attr_reader :tasks, :task, :language, :accessibility

  def initialize
    @tasks = TasksValidator.new
    @task = TaskValidator.new
    @language = LanguageValidator.new
    @accessibility = AccessibilityValidator.new
  end
end
