# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'

RSpec.describe 'Task table - accessibility', type: :system, accessibility: true,
                                              epic: AllureEpic::ACCESSIBILITY,
                                              feature: 'Task table',
                                              tms: '98' do
  before do
    support.mock.api.get_tasks([
                                 TaskContext.new.create_task_response,
                                 TaskContext.new.create_task_response
                               ])
  end

  it 'should have no accessibility violations on task table' do
    # given
    step.navigation.open_main_page

    # when
    results = step.accessibility.analyze

    # then
    validate.accessibility.has_no_violations(results)
  end
end
