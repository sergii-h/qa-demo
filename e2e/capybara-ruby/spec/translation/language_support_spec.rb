# frozen_string_literal: true

require 'spec_helper'

require_relative '../../context/task_context'
require_relative '../../data/allure_epic'
require_relative '../../data/task_priority'
require_relative '../../data/task_status'

RSpec.describe 'Language support', type: :system,
                                   epic: AllureEpic::TRANSLATION,
                                   feature: 'Language support',
                                   tms: '104' do
  let(:context) do
    TaskContext.new(status: TaskStatus::TODO, priority: TaskPriority::LOW)
  end

  before do
    response = context.create_task_response
    support.mock.api
         .get_tasks([response])
         .create_task(response)
  end

  it 'should switch UI to Spanish when ES is selected' do
    # given
    step.navigation.open_main_page

    # when
    step.language.select_language('ES')

    # then
    validate.language.ui_is_in_spanish
    validate.language.status_tag_shows_text('TODO', 'Por hacer')
    validate.language.priority_tag_shows_text('LOW', 'Baja')
  end
end
