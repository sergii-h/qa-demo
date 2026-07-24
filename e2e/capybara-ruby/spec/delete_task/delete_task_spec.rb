# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'

RSpec.describe 'Delete task', type: :system,
                              epic: AllureEpic::TASK_MANAGEMENT,
                              feature: 'Delete task',
                              tms: '99' do
  let(:context) { TaskContext.new }

  before do
    response = context.create_task_response
    support.mock.api
         .get_tasks([response])
         .delete_task(response[:id])
  end

  it 'should delete task' do
    # given
    step.navigation.open_main_page

    # when
    step.tasks.delete_task(context.title)

    # then
    validate.tasks.has_no_task(context.title)
  end
end
