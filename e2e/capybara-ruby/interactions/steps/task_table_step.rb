# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/main_page'
require_relative '../pages/create_task_form'
require_relative '../pages/task_info_modal'
require_relative '../pages/edit_task_form'
require_relative 'create_task_step'
require_relative 'edit_task_step'

class TaskTableStep
  extend StepDecorator::Methods
  include CapybaraHelpers

  attr_reader :create_task, :edit_task

  def initialize
    @main_page = MainPage.new
    @create_task_form = CreateTaskForm.new
    @task_info_modal = TaskInfoModal.new
    @edit_task_form = EditTaskForm.new
    @create_task = CreateTaskStep.new
    @edit_task = EditTaskStep.new
  end

  step 'Open create task form'
  def open_create_task_form
    @main_page.create_task_button.click
    expect(@create_task_form.submit_button).to be_visible
    @create_task
  end

  step "Open task info for task '{title}'"
  def open_task_info_form(title)
    id = resolve_task_id(title)
    @main_page.task_info_button_by_id(id).click
    expect(@task_info_modal.title).to be_visible
  end

  step "Open edit form for task '{title}'"
  def open_edit_task_form(title)
    id = resolve_task_id(title)
    @main_page.task_edit_button_by_id(id).click
    expect(@edit_task_form.title_input.value).to eq(title)
    @edit_task
  end

  step "Delete task '{title}'"
  def delete_task(title)
    id = resolve_task_id(title)
    @main_page.task_delete_button_by_id(id).click
    expect(page).to have_no_css("[data-testid=\"delete-button-#{id}\"]")
  end

  private

  def resolve_task_id(title)
    @main_page.task_title_by_title(title)['data-testid'].delete_prefix('task-title-')
  end
end
