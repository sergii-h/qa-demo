# frozen_string_literal: true

require_relative '../../support/capybara_helpers'

class CreateTaskForm
  include CapybaraHelpers
  def title_input
    find('[data-testid="create-task-title-input"]')
  end

  def description_input
    find('#description')
  end

  def status_dropdown
    find('[data-testid="status-dropdown"]')
  end

  def priority_dropdown
    find('[data-testid="priority-dropdown"]')
  end

  def submit_button
    find('[data-testid="create-button"]')
  end

  def status_option(status)
    find("[data-testid=\"status-dropdown-option-#{status}\"]", visible: :all)
  end

  def priority_option(priority)
    find("[data-testid=\"priority-dropdown-option-#{priority}\"]", visible: :all)
  end
end
