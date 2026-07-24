# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/task_info_modal'

class TaskValidator
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @modal = TaskInfoModal.new
  end

  step 'Validate task info data: {taskData}'
  def data(task)
    expect(@modal.title).to have_text(task[:title])
    expect(@modal.description).to have_text(task[:description])
    expect(@modal.status_tag(task[:status])).to be_visible
    expect(@modal.priority_tag(task[:priority])).to be_visible
    self
  end

  step 'Validate task is marked as valid'
  def is_valid
    expect(@modal.validation_badge).to be_visible
    self
  end

  step 'Validate task is marked as not valid'
  def is_not_valid
    expect(page).not_to have_css('[data-testid="valid"]')
    self
  end
end
