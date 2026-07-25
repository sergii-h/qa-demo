# frozen_string_literal: true

require 'spec_helper'

require_relative '../../data/allure_epic'

RSpec.describe 'Create task form - accessibility', type: :system, accessibility: true,
                                                    epic: AllureEpic::ACCESSIBILITY,
                                                    feature: 'Create task',
                                                    tms: '100' do
  it 'should have no accessibility violations on create task form' do
    # given
    step.navigation.open_main_page
    step.tasks.open_create_task_form

    # when
    results = step.accessibility.analyze

    # then
    validate.accessibility.has_no_violations(results)
  end
end
