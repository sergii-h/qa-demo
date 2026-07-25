# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'

RSpec.describe 'Create task', type: :system,
                             epic: AllureEpic::TASK_MANAGEMENT,
                             feature: 'Create task',
                             tms: '100' do
  let(:context) { TaskContext.new }

  before do
    response = context.create_task_response
    support.mock.api
         .get_tasks([response])
         .create_task(response)
         .get_task(response[:id], response)
         .get_is_valid(response[:id], true)
  end

  it 'should create task' do
    # given
    step.navigation.open_main_page

    # when
    step.tasks.open_create_task_form
         .fill_form(context.create_task_data)
         .submit_form

    # then
    validate.tasks.has_task(context.title)

    # when
    step.tasks.open_task_info_form(context.title)

    # then
    validate.task.data(context.create_task_data)
  end
end
