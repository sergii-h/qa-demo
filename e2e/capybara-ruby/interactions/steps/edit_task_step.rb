# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/edit_task_form'

class EditTaskStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @form = EditTaskForm.new
  end

  step 'Fill edit task form with {taskData}'
  def fill_form(task_data)
    fill_input(@form.title_input, task_data[:title]) if task_data[:title]
    fill_input(@form.description_input, task_data[:description]) if task_data[:description]
    if task_data[:status]
      @form.status_dropdown.click
      @form.status_option(task_data[:status]).click
    end
    if task_data[:priority]
      @form.priority_dropdown.click
      @form.priority_option(task_data[:priority]).click
    end
    self
  end

  step 'Submit edit task form'
  def submit_form
    @form.submit_button.click
    expect(page).to have_no_css('[data-testid="save-button"]', visible: true)
    self
  end

  private

  def fill_input(element, value)
    element.click
    element.send_keys([:control, 'a'], :backspace) unless element.value.empty?
    element.send_keys(value)
  end
end
