# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'
require_relative '../../data/task_priority'
require_relative '../../data/task_status'

RSpec.describe 'Edit task', type: :system,
                            epic: AllureEpic::TASK_MANAGEMENT,
                            feature: 'Edit task',
                            tms: '101' do
  let(:context) do
    TaskContext.new(status: TaskStatus::TODO, priority: TaskPriority::MEDIUM)
  end

  before do
    response = context.create_task_response
    support.mock.api
         .get_tasks([response])
         .get_task(response[:id], response)
         .get_is_valid(response[:id], true)
  end

  it 'should edit task' do
    # given
    updated_context = TaskContext.new(
      id: context.id,
      title: "#{context.title}-Updated",
      description: "#{context.description}-Updated",
      status: TaskStatus::IN_PROGRESS,
      priority: TaskPriority::HIGH
    )
    updated_response = updated_context.create_task_response

    step.navigation.open_main_page

    # when
    step.tasks.open_edit_task_form(context.title)
    step.tasks.edit_task.fill_form(updated_context.create_task_data)

    support.mock.api
         .update_task(updated_context.id, updated_response)
         .get_tasks([updated_response])
         .get_task(updated_response[:id], updated_response)
         .get_is_valid(updated_response[:id], true)

    step.tasks.edit_task.submit_form
    step.tasks.open_task_info_form(updated_context.title)

    # then
    validate.task.data(updated_context.create_task_data)
  end
end
