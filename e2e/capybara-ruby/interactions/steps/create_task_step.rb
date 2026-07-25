# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/create_task_form'

class CreateTaskStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @form = CreateTaskForm.new
  end

  step 'Fill create task form with {taskData}'
  def fill_form(task_data)
    fill_input(@form.title_input, task_data[:title])
    fill_input(@form.description_input, task_data[:description])
    @form.status_dropdown.click
    @form.status_option(task_data[:status]).click
    @form.priority_dropdown.click
    @form.priority_option(task_data[:priority]).click
    self
  end

  step 'Submit create task form'
  def submit_form
    @form.submit_button.click
    expect(page).to have_no_css('[data-testid="create-button"]', visible: true)
    self
  end

  private

  def fill_input(element, value)
    element.click
    element.send_keys(value)
  end
end
