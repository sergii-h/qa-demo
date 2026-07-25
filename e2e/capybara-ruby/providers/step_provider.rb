# frozen_string_literal: true

require_relative '../interactions/steps/accessibility_step'
require_relative '../interactions/steps/create_task_step'
require_relative '../interactions/steps/edit_task_step'
require_relative '../interactions/steps/language_switcher_step'
require_relative '../interactions/steps/navigation_step'
require_relative '../interactions/steps/task_table_step'

class StepProvider
  attr_reader :navigation, :tasks, :create_task, :edit_task, :language, :accessibility

  def initialize
    @navigation = NavigationStep.new
    @tasks = TaskTableStep.new
    @create_task = CreateTaskStep.new
    @edit_task = EditTaskStep.new
    @language = LanguageSwitcherStep.new
    @accessibility = AccessibilityStep.new
  end
end
