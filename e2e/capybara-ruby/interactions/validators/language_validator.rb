# frozen_string_literal: true

require_relative '../../decorators/step_decorator'
require_relative '../../support/capybara_helpers'
require_relative '../pages/main_page'

class LanguageValidator
  extend StepDecorator::Methods
  include CapybaraHelpers

  def initialize
    @main_page = MainPage.new
  end

  step 'Validate UI is in Spanish'
  def ui_is_in_spanish
    expect(@main_page.create_task_button).to have_text('Crear tarea')
    expect(@main_page.table_headers).to have_text('Título')
    expect(@main_page.table_headers).to have_text('Estado')
    expect(@main_page.table_headers).to have_text('Prioridad')
  end

  step 'Validate status tag for {status} shows {expectedText}'
  def status_tag_shows_text(status, expected_text)
    expect(@main_page.status_tag(status)).to have_text(expected_text)
  end

  step 'Validate priority tag for {priority} shows {expectedText}'
  def priority_tag_shows_text(priority, expected_text)
    expect(@main_page.priority_tag(priority)).to have_text(expected_text)
  end
end
