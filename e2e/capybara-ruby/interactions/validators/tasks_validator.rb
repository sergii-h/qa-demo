# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/main_page'

class TasksValidator
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @main_page = MainPage.new
  end

  step 'Validate task with title {title} is visible'
  def has_task(title)
    expect(@main_page.task_row_by_title(title)).to be_visible
    self
  end

  step 'Validate task with title {title} is not visible'
  def has_no_task(title)
    expect(@main_page.task_table_body).not_to have_text(title)
    self
  end

  step 'Validate task count is {count}'
  def has_task_count(count)
    if count.zero?
      expect(page).not_to have_css('[data-testid^="task-title-"]')
    else
      expect(@main_page.task_rows.size).to eq(count)
    end
    self
  end
end
