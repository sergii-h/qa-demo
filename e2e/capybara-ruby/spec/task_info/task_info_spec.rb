# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'

RSpec.describe 'View task info', type: :system,
                                 epic: AllureEpic::TASK_MANAGEMENT,
                                 feature: 'View task info',
                                 tms: '102' do
  let(:context) { TaskContext.new }

  before do
    response = context.create_task_response
    support.mock.api
         .get_tasks([response])
         .get_task(response[:id], response)
         .get_is_valid(response[:id], true)
  end

  it 'should view task info' do
    # given
    step.navigation.open_main_page

    # when
    step.tasks.open_task_info_form(context.title)

    # then
    validate.task.data(context.create_task_data)
    validate.task.is_valid
  end
end
