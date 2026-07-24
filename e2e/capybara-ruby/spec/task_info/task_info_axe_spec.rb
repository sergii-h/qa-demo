# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'

RSpec.describe 'View task info - accessibility', type: :system, accessibility: true,
                                                  epic: AllureEpic::ACCESSIBILITY,
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

  it 'should have no accessibility violations on task info modal' do
    # given
    step.navigation.open_main_page
    step.tasks.open_task_info_form(context.title)

    # when
    results = step.accessibility.analyze

    # then
    validate.accessibility.has_no_violations(results)
  end
end
